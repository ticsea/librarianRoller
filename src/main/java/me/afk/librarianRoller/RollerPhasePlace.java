package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.Librarians;
import me.afk.librarianRoller.utils.PlayerInventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * PLACE phase - a stateless pure executor.
 * <p>
 * Waits for the lectern to be back in the player's inventory, equips it in the
 * offhand, and places it on the current villager's lectern block target. Reads
 * the pickup-wait and place-failure counters from the context (read-only) and
 * emits events; the context owns the counter increments and failure handling.
 */
public class RollerPhasePlace implements IRollerPhase {
    private static final int MAX_PICKUP_WAIT_TICKS = 40;
    private static final int MAX_PLACE_FAILURES = 8;

    private final RollerState state;
    private final RollerTransitions transitions;

    public RollerPhasePlace(RollerState state, RollerTransitions transitions) {
        this.state = state;
        this.transitions = transitions;
    }

    @Override
    public RollerEvent doAction() {
        if (!state.isEnabled()) return RollerEvent.of(RollerEvent.Type.WAITING);
        Minecraft instance = state.getMinecraft();
        if (instance == null) return RollerEvent.of(RollerEvent.Type.WAITING);
        LocalPlayer player = instance.player;
        MultiPlayerGameMode interactionManager = instance.gameMode;
        Level level = instance.level;
        int pairIndex = state.getPairIndex();
        List<Librarians> list = state.getList();

        if (player == null || level == null || list.isEmpty() || interactionManager == null) {
            return RollerEvent.of(RollerEvent.Type.WAITING);
        }

        if (!player.getOffhandItem().is(Items.LECTERN)) {
            // Wait a short while before giving up, instead of erroring out on the first tick.
            if (!RollerUtils.hasLecternInInventory(player)) {
                // +1 accounts for the current tick: the context increments the counter
                // AFTER this event, so the timeout fires on the 40th waiting tick.
                if (state.getPickupWaitTicks() + 1 >= MAX_PICKUP_WAIT_TICKS) {
                    // The lectern never came back - treat as a place failure for this pair.
                    return RollerEvent.of(RollerEvent.Type.PLACE_FAILED);
                }
                // Context increments the pickup-wait counter on WAITING.
                return RollerEvent.of(RollerEvent.Type.WAITING);
            }
            if (!PlayerInventoryUtils.swapItem(player, EquipmentSlot.OFFHAND, item -> item == Items.LECTERN)) {
                // swapItem failure: lectern vanished before it could be equipped - fatal.
                return RollerEvent.of(RollerEvent.Type.FATAL);
            }
        }

        BlockHitResult hitResult = list.get(pairIndex).lecternBelowHitResult();
        if (hitResult.getType() == HitResult.Type.MISS) {
            return RollerEvent.of(RollerEvent.Type.FATAL);
        }

        var result = interactionManager.useItemOn(player, InteractionHand.OFF_HAND, hitResult);

        if (result == InteractionResult.SUCCESS) {
            // The context resets both the place-failure counter and the consecutive-failure
            // counter here, then advances to the next villager.
            return RollerEvent.of(RollerEvent.Type.PLACE_SUCCESS);
        } else {
            // Stay in the PLACE phase and retry, instead of bouncing to INTERACT forever.
            if (state.getPlaceFailures() >= MAX_PLACE_FAILURES) {
                return RollerEvent.of(RollerEvent.Type.PLACE_FAILED);
            }
            // Context increments the place-failure counter on PLACE_RETRY.
            return RollerEvent.of(RollerEvent.Type.PLACE_RETRY);
        }
    }
}
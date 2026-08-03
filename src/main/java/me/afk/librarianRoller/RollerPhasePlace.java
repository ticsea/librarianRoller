package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import me.afk.librarianRoller.utils.MessageUtils;
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

public class RollerPhasePlace implements IRollerPhase {
    // P0 (FIXME 1): how many ticks to wait for a broken lectern to enter the inventory.
    private static final int MAX_PICKUP_WAIT_TICKS = 40;
    // P0 (FIXME 2): how many consecutive place failures before we skip the current villager.
    private static final int MAX_PLACE_FAILURES = 8;

    private final RollerState state;
    private final RollerTransitions transitions;
    // P0: local state - ticks spent waiting for the lectern pickup.
    private int pickupWaitTicks = 0;
    // P0: local state - consecutive failed place attempts for the current villager.
    private int placeFailures = 0;

    public RollerPhasePlace(RollerState state, RollerTransitions transitions) {
        this.state = state;
        this.transitions = transitions;
    }

    @Override
    public void onReset() {
        this.pickupWaitTicks = 0;
        this.placeFailures = 0;
    }

    @Override
    public void doAction() {
        if (!state.isEnabled()) return;
        Minecraft instance = state.getMinecraft();
        LocalPlayer player = instance.player;
        MultiPlayerGameMode interactionManager = instance.gameMode;
        Level level = instance.level;
        int pairIndex = state.getPairIndex();
        List<VillagerAndLectern> list = state.getList();

        if (player == null || level == null || list.isEmpty() || interactionManager == null) return;


        if (!player.getOffhandItem().is(Items.LECTERN)) {
            // P0 (FIXME 1): the lectern may not have entered the inventory yet (pickup delay).
            // Wait a short while before giving up, instead of erroring out on the first tick.
            if (!RollerUtils.hasLecternInInventory(player)) {
                this.pickupWaitTicks++;
                if (this.pickupWaitTicks >= MAX_PICKUP_WAIT_TICKS) {
                    handlePairFailure();
                }
                return;
            }
            // P0 (FIXME 1): the lectern is in the inventory but not the offhand - swap it there.
            if (!PlayerInventoryUtils.swapItem(player, EquipmentSlot.OFFHAND, item -> item == Items.LECTERN)) {
                MessageUtils.throwError("afk.enchant_roller.error.not_found_lectern");
                transitions.stop();
                return;
            }
            this.pickupWaitTicks = 0;
        }

        BlockHitResult hitResult = list.get(pairIndex).lecternBelowHitResult();
        if (hitResult.getType() == HitResult.Type.MISS) {
            transitions.stop();
            return;
        }

        var result = interactionManager.useItemOn(player, InteractionHand.OFF_HAND, hitResult);

        if (result == InteractionResult.SUCCESS) {
            // P0 (FIXME 2): placing succeeded - a full villager cycle completed.
            // Reset both the place-failure counter and the consecutive-failure counter here.
            this.placeFailures = 0;
            transitions.onPairSuccess();
            transitions.advancePair();
            transitions.transitionTo(transitions.getInteract());
        } else {
            // P0 (FIXME 2): placing failed (e.g. server hasn't confirmed the block is gone).
            // Stay in the Place phase and retry, instead of bouncing to Interact forever.
            this.placeFailures++;
            if (this.placeFailures >= MAX_PLACE_FAILURES) {
                handlePairFailure();
            }
        }
    }

    // P0: record a place failure, skip the current villager and eventually give up.
    private void handlePairFailure() {
        this.placeFailures = 0;
        this.pickupWaitTicks = 0;
        transitions.onPairFailure();
        if (transitions.shouldStopAfterTooManyFailures()) {
            MessageUtils.throwError("afk.enchant_roller.error.lectern_place_failed");
            transitions.stop();
            return;
        }
        // Skip this pair and move on to the next villager.
        transitions.advancePair();
        transitions.transitionTo(transitions.getInteract());
    }
}
package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.dataModel.Librarians;
import me.afk.librarianRoller.utils.PlayerInventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;

import java.util.List;

/**
 * BREAK phase - a stateless pure executor.
 * <p>
 * Destroys the current villager's lectern and waits for it to reappear in the
 * player's inventory (server-verified pickup). Reads the pickup-wait tick counter
 * from the context (read-only) and emits events; the context owns the counter
 * increments and the timeout side effects.
 * <p>
 * Note: on pickup success this phase does NOT emit a "pair success" - that only
 * happens when a full villager cycle completes (place succeeds and we advance).
 */
public class RollerPhaseBreak implements IRollerPhase {
    private static final int MAX_PICKUP_WAIT_TICKS = 40;

    private final RollerState state;
    private final RollerTransitions transitions;

    public RollerPhaseBreak(RollerState state, RollerTransitions transitions) {
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
        ModConfig modConfig = transitions.getModConfig();

        if (player == null || level == null || list.isEmpty() || interactionManager == null) {
            return RollerEvent.of(RollerEvent.Type.WAITING);
        }
        if (!PlayerInventoryUtils.swapAxe(player)) {
            // No axe available right now - stop.
            return RollerEvent.of(RollerEvent.Type.FATAL);
        }
        if (PlayerInventoryUtils.preventAxeBreaking(player, modConfig)) {
            // Pure detection - the phase emits FATAL and the context stops the roller.
            return RollerEvent.of(RollerEvent.Type.FATAL);
        }

        var currentPair = list.get(pairIndex);
        BlockPos lecternPos = currentPair.lecternPos();
        var block = level.getBlockState(lecternPos).getBlock();

        if (block instanceof LecternBlock) {
            if (interactionManager.continueDestroyBlock(lecternPos, Direction.UP)) {
                player.swing(InteractionHand.MAIN_HAND);
            }
            // Still destroying - do not count pickup-wait ticks.
            return RollerEvent.of(RollerEvent.Type.BREAKING);
        }

        // Lectern block is gone. It may not have entered the player's inventory yet
        // (server-verified pickup). Wait for it before placing again to avoid racing
        // the drop pickup.
        if (!RollerUtils.hasLecternInInventory(player)) {
            // +1 accounts for the current tick: the context increments the counter
            // AFTER this event, so the timeout fires on the 40th waiting tick
            // (matching the original pre-increment semantics).
            if (state.getPickupWaitTicks() + 1 >= MAX_PICKUP_WAIT_TICKS) {
                // Pickup timed out -> likely the lectern dropped somewhere unreachable.
                // The context records the failure and skips this pair.
                return RollerEvent.of(RollerEvent.Type.PICKUP_TIMEOUT);
            }
            // Context increments the pickup-wait counter on WAITING.
            return RollerEvent.of(RollerEvent.Type.WAITING);
        }

        // Success: the lectern is safely in the inventory again.
        // NOTE: no "pair success" here - pickup is only a mid-cycle step. The
        // consecutive-failure counter is reset only when a full villager cycle
        // completes (place succeeds and we advance to the next villager).
        return RollerEvent.of(RollerEvent.Type.PICKUP_COMPLETE);
    }
}
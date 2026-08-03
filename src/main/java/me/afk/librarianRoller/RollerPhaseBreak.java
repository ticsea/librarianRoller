package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import me.afk.librarianRoller.utils.MessageUtils;
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

public class RollerPhaseBreak implements IRollerPhase {
    // P0 (FIXME 1): how many ticks to wait for a broken lectern to enter the inventory.
    private static final int MAX_PICKUP_WAIT_TICKS = 40;

    private final RollerState state;
    private final RollerTransitions transitions;
    // P0: local state - ticks spent waiting for the lectern pickup.
    private int pickupWaitTicks = 0;

    public RollerPhaseBreak(RollerState state, RollerTransitions transitions) {
        this.state = state;
        this.transitions = transitions;
    }

    @Override
    public void onReset() {
        this.pickupWaitTicks = 0;
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
        ModConfig modConfig = transitions.getModConfig();

        if (player == null || level == null || list.isEmpty() || interactionManager == null) return;
        if (PlayerInventoryUtils.swapAxe(player)) return;
        if (PlayerInventoryUtils.preventAxeBreaking(transitions, player, modConfig)) return;

        var currentPair = list.get(pairIndex);
        BlockPos lecternPos = currentPair.lecternPos();
        var block = level.getBlockState(lecternPos).getBlock();

        // or blockState.getBlock() != Blocks.AIR?
        if (block instanceof LecternBlock) {

            if (interactionManager.continueDestroyBlock(lecternPos, Direction.UP)) {
                player.swing(InteractionHand.MAIN_HAND);
            }

        } else {
            // P0 (FIXME 1): the lectern block is gone client-side, but the dropped lectern
            // may not have entered the player's inventory yet (server-verified pickup).
            // Wait for it before placing again to avoid racing the drop pickup.
            if (!RollerUtils.hasLecternInInventory(player)) {
                this.pickupWaitTicks++;
                if (this.pickupWaitTicks >= MAX_PICKUP_WAIT_TICKS) {
                    // Pickup timed out -> likely the lectern dropped somewhere unreachable.
                    handlePairFailure();
                }
                return;
            }

            // Success: the lectern is safely in the inventory again.
            // NOTE: do NOT call onPairSuccess() here - pickup is only a mid-cycle step.
            // The consecutive-failure counter is reset only when a full villager cycle
            // completes (place succeeds and we advance to the next villager).
            this.pickupWaitTicks = 0;
            transitions.transitionTo(transitions.getPlace());
        }
    }

    // P0: record a pickup failure, skip the current villager and eventually give up.
    private void handlePairFailure() {
        this.pickupWaitTicks = 0;
        transitions.onPairFailure();
        if (transitions.shouldStopAfterTooManyFailures()) {
            MessageUtils.throwError("afk.enchant_roller.error.lectern_pickup_failed");
            transitions.stop();
            return;
        }
        // Skip this pair and move on to the next villager.
        transitions.advancePair();
        transitions.transitionTo(transitions.getInteract());
    }
}
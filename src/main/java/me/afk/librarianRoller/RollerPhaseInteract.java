package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import me.afk.librarianRoller.utils.PlayerInventoryUtils;
import me.afk.librarianRoller.utils.VillagerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if > 1.21.1 {
import net.minecraft.world.entity.npc.villager.Villager;
//?} else {

/*import net.minecraft.world.entity.npc.Villager;
        *///?}
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class RollerPhaseInteract implements IRollerPhase {
    private final RollerState state;
    private final RollerTransitions transitions;

    public RollerPhaseInteract(RollerState state, RollerTransitions transitions) {
        this.state = state;
        this.transitions = transitions;
    }

    @Override
    public void doAction() {
        if (!state.isEnabled()) return;

        Minecraft instance = state.getMinecraft();
        if (instance == null) return;
        LocalPlayer player = instance.player;
        MultiPlayerGameMode gameMode = instance.gameMode;
        Level level = instance.level;
        if (player == null || gameMode == null || level == null) return;
        List<VillagerAndLectern> list = state.getList();
        if (list.isEmpty()) return;
        int pairIndex = state.getPairIndex();
        if (pairIndex >= list.size()) {
            // Wrap around defensively.
            transitions.transitionTo(transitions.getInteract());
            return;
        }

        // two case if result is not success
        // 1. the villager is unemploy but lectern exit
        // 2. the lectern is gone
        VillagerAndLectern villagerAndLectern = list.get(pairIndex);
        if (villagerAndLectern == null) return;
        BlockPos blockPos = villagerAndLectern.lecternPos();
        if (blockPos == null) return;
        Block block = level.getBlockState(blockPos).getBlock();
        if (block == Blocks.AIR) {
            transitions.transitionTo(transitions.getPlace());
            return;
        }

        Villager villager = villagerAndLectern.villager();
        if (villager == null || !VillagerUtils.isLibrarian(villager)) return;

        if (PlayerInventoryUtils.swapAxe(player)) return;

        // P1: signal the Mixin that an automated interaction is in flight, so it cancels
        // the resulting OpenScreen (the roller runs headless).
        transitions.getScreenIntent().setAwaitingMerchantScreen(true);
        InteractionResult result = gameMode.interact(player, villager, InteractionHand.MAIN_HAND);
        player.swing(InteractionHand.MAIN_HAND);

        if (result == InteractionResult.SUCCESS) {
            transitions.transitionTo(transitions.getParse());
        } else {
            // Interaction failed - no OpenScreen will arrive, clear the intent flag.
            transitions.getScreenIntent().setAwaitingMerchantScreen(false);
        }
    }
}
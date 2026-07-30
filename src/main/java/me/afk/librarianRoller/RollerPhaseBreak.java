package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.dataModel.VillagerAndLectern;
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

public class RollerPhaseBreak implements IRollerPhase{
    public static final RollerPhaseBreak INSTANCE = new RollerPhaseBreak();

    private RollerPhaseBreak() {
    }

    @Override
    public void doAction(RollerContext ctx) {
        if (!ctx.getEnabled()) return;

        Minecraft instance = ctx.getMinecraft();
        LocalPlayer player = instance.player;
        MultiPlayerGameMode interactionManager = instance.gameMode;
        Level level = instance.level;
        int pairIndex = ctx.getPairIndex();
        List<VillagerAndLectern> list = ctx.getList();
        ModConfig modConfig = ctx.getModConfig();

        if (player == null || level == null || list.isEmpty() || interactionManager == null) return;
        if (PlayerInventoryUtils.swapAxe(ctx, player)) return;
        if (PlayerInventoryUtils.preventAxeBreaking(ctx, player, modConfig)) return;

        var currentPair = list.get(pairIndex);
        BlockPos lecternPos = currentPair.lecternPos();
        var block = level.getBlockState(lecternPos).getBlock();

        // or blockState.getBlock() != Blocks.AIR?
        if (block instanceof LecternBlock) {

            if (interactionManager.continueDestroyBlock(lecternPos, Direction.UP)) {
                player.swing(InteractionHand.MAIN_HAND);
            }

        } else {
            ctx.setRollerPhase(RollerPhasePlace.INSTANCE);
        }
    }


}

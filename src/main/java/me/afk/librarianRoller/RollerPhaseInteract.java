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
/*import net.minecraft.world.entity.npc.villager.Villager;
*///?} else {

import net.minecraft.world.entity.npc.Villager;
        //?}
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class RollerPhaseInteract implements IRollerPhase{
    public static final RollerPhaseInteract INSTANCE = new RollerPhaseInteract();

    private RollerPhaseInteract() {
    }

    @Override
    public void doAction(RollerContext ctx) {
        if (!ctx.getEnabled()) return;

//        Minecraft instance = Minecraft.getInstance();
        Minecraft instance = ctx.getMinecraft();
        if (instance == null) return;
        LocalPlayer player = instance.player;
        MultiPlayerGameMode gameMode = instance.gameMode;
        Level level = instance.level;
        if (player == null || gameMode == null || level == null) return;
        List<VillagerAndLectern> list = ctx.getList();
        if (list.isEmpty()) return;
        int pairIndex = ctx.getPairIndex();
        if (pairIndex >= list.size()) {
            ctx.setPairIndex(0);
            return;
        }
        //        ModConfig modConfig = ctx.getModConfig();


        // two case if result is not success
        // 1. the villager is unemploy but lectern exit
        // 2. the lectern is gone
        VillagerAndLectern villagerAndLectern = list.get(pairIndex);
        if (villagerAndLectern == null) return;
        BlockPos blockPos = villagerAndLectern.lecternPos();
        if (blockPos == null )return;
        Block block = level.getBlockState(blockPos).getBlock();
        if (block == Blocks.AIR) {
            ctx.setRollerPhase(RollerPhasePlace.INSTANCE);
            return;
        }

        Villager villager = villagerAndLectern.villager();
        if (villager == null || !VillagerUtils.isLibrarian(villager)) return;

        if (PlayerInventoryUtils.swapAxe(ctx, player)) return;

        InteractionResult result = gameMode.interact(player, villager, InteractionHand.MAIN_HAND);
        player.swing(InteractionHand.MAIN_HAND);

        if (result == InteractionResult.SUCCESS) {
            ctx.setRollerPhase(RollerPhaseParse.INSTANCE);
        }
    }
}

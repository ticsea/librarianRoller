package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.utils.MessageUtils;
import me.afk.librarianRoller.utils.PlayerInventoryUtils;
import me.afk.librarianRoller.utils.VillagerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.AxeItem;
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
        LocalPlayer player = instance.player;
        MultiPlayerGameMode interactionManager = instance.gameMode;
        Level level = instance.level;
        int pairIndex = ctx.getPairIndex();
        List<VillagerAndLectern> list = ctx.getList();
//        ModConfig modConfig = ctx.getModConfig();

        if (player == null || level == null || list.isEmpty() || interactionManager == null) return;

        // two case if result is not success
        // 1. the villager is unemploy but lectern exit
        // 2. the lectern is gone
        BlockPos blockPos = list.get(pairIndex).lecternPos();
        Block block = level.getBlockState(blockPos).getBlock();
        if (block == Blocks.AIR) {
            ctx.setRollerPhase(RollerPhasePlace.INSTANCE);
        }

        Villager villager = list.get(pairIndex).villager();
        if (!VillagerUtils.isLibrarian(villager)) return;

        if (PlayerInventoryUtils.swapAxe(ctx, player)) return;

        InteractionResult result = interactionManager.interact(player, villager, InteractionHand.MAIN_HAND);
        player.swing(InteractionHand.MAIN_HAND);

        /*if (result == InteractionResult.SUCCESS) {
            ctx.setRollerPhase(RollerPhaseBreak.INSTANCE);
            return;
        }*/
    }
}

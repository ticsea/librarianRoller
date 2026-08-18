package me.afk.librarianRoller.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
//~ if >= 1.21.11 'net.minecraft.world.entity.npc.Villager' -> 'net.minecraft.world.entity.npc.villager.Villager' {
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.EntityHitResult;
        //~}

public class InteractionUtils {
    public static InteractionResult interactVillager(LocalPlayer player, Villager villager) {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) return InteractionResult.FAIL;

        //? if >= 26.1 {
        /*return gameMode.interact(player, villager, new EntityHitResult(villager), InteractionHand.MAIN_HAND);
        *///?} else {
        return gameMode.interact(player, villager, InteractionHand.MAIN_HAND);
        //?}
    }
}

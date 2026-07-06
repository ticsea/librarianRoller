package me.afk.librarianRoller;

import net.minecraft.core.BlockPos;
        //? if >=1.21.11 {
import net.minecraft.world.entity.npc.villager.Villager;
        //?} else {
/*import net.minecraft.world.entity.npc.Villager;
*///?}
import net.minecraft.world.phys.BlockHitResult;

public record VillagerAndLectern(Villager villager, BlockPos lecternPos, BlockHitResult lecternBelowHitResult) {
}

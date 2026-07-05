package me.afk.librarianRoller.utils;

        //? if >=1.21.11 {
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
        //?} else {
/*import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
*///?}

public class VillagerUtils {

    public static boolean isCorrectVillagerProfession(Villager villager) {
        //? if >= 1.21.11 {
        return villager.getVillagerData().profession().is(VillagerProfession.LIBRARIAN);
        //?} else {
        /*return villager.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN;
        *///?}
    }
}

package me.afk.librarianRoller.utils;

        //? if >=1.21.11 {
/*import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
        *///?} else {
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
//?}

public class VillagerUtils {

    public static boolean isLibrarian(Villager villager) {
        //? if >= 1.21.11 {
        /*//todo it does not work about villager.getVillagerXp() < 1 if just buy few times.
        return villager.getVillagerData().profession().is(VillagerProfession.LIBRARIAN) && villager.getVillagerXp() < 1;
        *///?} else {
        return villager.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN;
        //?}
    }
}

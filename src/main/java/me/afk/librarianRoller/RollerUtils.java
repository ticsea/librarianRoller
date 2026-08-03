package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Stateless helper methods for the roller state machine. Kept out of RollerContext so the
 * context stays a pure state container.
 */
public final class RollerUtils {
    private RollerUtils() {
    }

    // (offhand, hotbar or main slots). Used before placing to avoid racing the broken-lectern pickup.
    public static boolean hasLecternInInventory(LocalPlayer player) {
        if (player == null) return false;
        if (player.getOffhandItem().is(Items.LECTERN)) return true;
        //~ if >= 1.21.11 'items' -> 'getNonEquipmentItems()' {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            //~}
            if (stack.is(Items.LECTERN)) return true;
        }
        return false;
    }

    /*public static boolean advancePair(RollerContext ctx) {
        List<VillagerAndLectern> list = ctx.getList();
        if (list.isEmpty()) return false;
        ctx.setPairIndex((ctx.getPairIndex() + 1) % list.size());
        return true;
    }*/
}
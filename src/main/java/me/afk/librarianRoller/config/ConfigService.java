package me.afk.librarianRoller.config;

import me.afk.librarianRoller.dataModel.Enchantment;
import me.afk.librarianRoller.utils.ItemEnchantmentParsingUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Business operations on the config: adding enchantments from the player's hand or
 * inventory to the target entry list. Saves through the normal AutoConfig API
 * (no ClothConfigScreen cast hack).
 */
public class ConfigService {
    public int addHandToEntry() {
        int times = 0;
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        if (player == null) return times;
        Inventory inventory = player.getInventory();
        //~ if >= 1.21.11 'getSelected' -> 'getSelectedItem' {
        ItemStack item = inventory.getSelectedItem();
        //~}
        List<Enchantment> enchantments = ItemEnchantmentParsingUtils.readStoredEnchantments(item);
        for (var book : enchantments) {
            if (addToEntry(book.name(), book.level())) {
                ++times;
            }
        }

        return times;
    }

    public int addAllToEntry() {
        int times = 0;
        Minecraft instance = Minecraft.getInstance();

        LocalPlayer player = instance.player;
        if (player == null) return times;
        //~ if>= 1.21.11 'items' -> 'getNonEquipmentItems()'{
        NonNullList<ItemStack> stacks = player.getInventory().getNonEquipmentItems();
        //~}
        for (var item : stacks) {
            var enchantedBooks = ItemEnchantmentParsingUtils.readStoredEnchantments(item);
            for (var book : enchantedBooks) {
                if (addToEntry(book.name(), book.level())) {
                    ++times;
                }
            }
        }

        return times;
    }

    private boolean addToEntry(String enchId, int lvl) {
        List<String> entry = getConfig().entry;
        String enchantment = enchId + ' ' + lvl;
        if (entry.contains(enchantment)) return false;
        entry.add(enchantment);
        // Save through the normal AutoConfig API - no ClothConfigScreen cast hack.
        AutoConfig.getConfigHolder(ModConfig.class).save();
        return true;
    }

    private ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
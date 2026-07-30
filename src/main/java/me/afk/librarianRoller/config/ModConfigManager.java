package me.afk.librarianRoller.config;

import me.afk.librarianRoller.LibrarianRoller;
import me.afk.librarianRoller.dataModel.Enchantment;
import me.afk.librarianRoller.utils.EnchantedBookUtils;
import me.afk.librarianRoller.utils.MessageUtils;
import me.afk.librarianRoller.utils.villagerAndLectern.IRollerMode;
import me.afk.librarianRoller.utils.villagerAndLectern.RollerModeRegistry;
import me.shedaniel.autoconfig.AutoConfig;
//? if >= 1.21.11 {
/*import me.shedaniel.autoconfig.AutoConfigClient;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
*///?} else if >= 1.20.1 {

        //?}
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModConfigManager {
    private final Map<String, Integer> ENTRY = new HashMap<>();

    public void registerConfig() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        //todo refact here
        //~ if >= 1.21.11 'AutoConfig' -> 'AutoConfigClient' {
        AutoConfig.getGuiRegistry(ModConfig.class).registerPredicateProvider(
                (i18n, field, config, defaults, registry) -> {
                    ModConfig modConfig = (ModConfig) config;
                    IRollerMode[] allModes = RollerModeRegistry.getRollerModes().toArray(new IRollerMode[0]);

                    IRollerMode currentMode = RollerModeRegistry.getRollerModes().stream()
                            .filter(m -> m.getName().equals(modConfig.rollerMode))
                            .findFirst()
                            .orElse(allModes[0]);

                    IRollerMode defaultMode = RollerModeRegistry.getRollerModes().stream()
                            .filter(m -> m.getName().equals(((ModConfig) defaults).rollerMode))
                            .findFirst()
                            .orElse(allModes[0]);

                    return Collections.singletonList(
                            ConfigEntryBuilder.create()
                                    .startSelector(Component.translatable(i18n), allModes, currentMode)
                                    .setNameProvider(mode -> Component.literal(mode.getName()))
                                    .setDefaultValue(defaultMode)
                                    .setSaveConsumer(newMode -> modConfig.rollerMode = newMode.getName())
                                    .build()
                    );
                },
                field -> field.getName().equals("rollerMode") && field.getType() == String.class
        );
        //~}
    }

    public ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public Screen getConfigScreen(Screen p) {
        //? if >= 1.21.11 {
        /*return AutoConfigClient.getConfigScreen(ModConfig.class, p).get();
        *///?} else {
        return AutoConfig.getConfigScreen(ModConfig.class, p).get();
         //?}
    }

    public Map<String, Integer> getEntry() {
        return ENTRY;
    }

    public void setEntry() {
        ENTRY.clear();

        getConfig().entry.forEach(it -> {
            // Split from the end - the last part is the level
            int lastSpaceIndex = it.lastIndexOf(' ');
            if (lastSpaceIndex == -1) return;

            String enchantmentName = it.substring(0, lastSpaceIndex).trim().toLowerCase();
            String levelStr = it.substring(lastSpaceIndex + 1).trim();

            if (levelStr.equals("0")) return;

            try {
                ENTRY.put(enchantmentName, Integer.valueOf(levelStr));
            } catch (NumberFormatException e) {
                // Invalid level
                return;
            }
        });
    }

    public void addHandToEntry() {
        int times = 0;
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        if (player == null ) return;
        Inventory inventory = player.getInventory();
        //~ if >= 1.21.11 'getSelected' -> 'getSelectedItem' {
        ItemStack item = inventory.getSelected();
    //~}
        List<Enchantment> enchantments = EnchantedBookUtils.readStoredEnchantments(item);
        for (var book : enchantments) {
            String enchId = book.name(); // e.g. "minecraft:mending"
            int lvl = book.level();

            if (addToEntry(enchId, lvl)) {
                ++times;
            }
        }

        MessageUtils.print("afk.enchant_roller.info.added_entry", Component.literal(String.valueOf(times)));
    }

    private boolean addToEntry(String enchId, int lvl) {
        List<String> entry = getConfig().entry;
        String enchantment = enchId + ' ' + lvl;
        if (entry.contains(enchantment)) return false;
        entry.add(enchantment);
        ClothConfigScreen configScreen = (ClothConfigScreen) LibrarianRoller.MODCONFIGMANAGER.getConfigScreen(null);
        configScreen.save();
        return true;
    }

    public void addAllToEntry() {
        int times = 0;
        Minecraft instance = Minecraft.getInstance();

        LocalPlayer player = instance.player;
        if (player == null ) return;
        //~ if>= 1.21.11 'items' -> 'getNonEquipmentItems()'{
        NonNullList<ItemStack> stacks = player.getInventory().items;
        //~}
        for (var item : stacks) {
            var enchantedBooks = EnchantedBookUtils.readStoredEnchantments(item);
            for (var book : enchantedBooks) {
                String enchId = book.name(); // e.g. "minecraft:mending"
                int lvl = book.level();

                if (addToEntry(enchId, lvl)) {
                    ++times;
                }
            }
        }

        MessageUtils.print("afk.enchant_roller.info.added_entry", Component.literal(String.valueOf(times)));
    }

    public void registerEntry() {
        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

        IRollerMode[] allModes = RollerModeRegistry.getRollerModes().toArray(new IRollerMode[0]);

        // Find current mode object from saved name
        IRollerMode currentMode = RollerModeRegistry.getRollerModes().stream()
                .filter(m -> m.getName().equals(getConfig().rollerMode))
                .findFirst()
                .orElse(allModes[0]);

        entryBuilder.startSelector(
                        Component.literal("Roller Mode"),
                        allModes,
                        currentMode
                )
                .setNameProvider(mode -> Component.literal(mode.getName()))
                .setDefaultValue(allModes[0])
                .setSaveConsumer(newMode -> getConfig().rollerMode = newMode.getName()) // save name string
                .build();
    }
}

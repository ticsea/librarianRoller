package me.afk.librarianRoller.config;

import me.shedaniel.autoconfig.AutoConfig;
//? if >= 1.21.11 {
import me.shedaniel.autoconfig.AutoConfigClient;
//?}
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.gui.screens.Screen;

import java.util.HashMap;
import java.util.Map;

public class ModConfigManager {
    private static Map<String, Integer> ENTRY = null;

    public static void registerConfig() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }

    public static ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static Screen getConfigScreen(Screen p) {
        //? if >= 1.21.11 {
        return AutoConfigClient.getConfigScreen(ModConfig.class, p).get();
        //?} else {
        /*return AutoConfig.getConfigScreen(ModConfig.class, p).get();
         *///?}
    }

    public static Map<String, Integer> getEntry() {
        if (ENTRY == null) {
            setEntry();
        }

        return ENTRY;
    }

    public static void setEntry() {
        var tempMap = new HashMap<String, Integer>();

        getConfig().entry.forEach(it -> {
            // Split from the end - the last part is the level
            int lastSpaceIndex = it.lastIndexOf(' ');
            if (lastSpaceIndex == -1) return;

            String enchantmentName = it.substring(0, lastSpaceIndex).trim().toLowerCase();
            String levelStr = it.substring(lastSpaceIndex + 1).trim();

            if (levelStr.equals("0")) return;

            try {
                tempMap.put(enchantmentName, Integer.valueOf(levelStr));
            } catch (NumberFormatException e) {
                // Invalid level
                return;
            }
        });

        ENTRY = tempMap;
    }
}

package me.afk.librarianRoller.config;

import me.afk.librarianRoller.utils.villagerAndLectern.IRollerMode;
import me.afk.librarianRoller.utils.villagerAndLectern.RollerModeRegistry;
import me.shedaniel.autoconfig.AutoConfig;
//? if >= 1.21.11 {
import me.shedaniel.autoconfig.AutoConfigClient;
//?}
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModConfigManager {
    private static ModConfigManager INSTANCE;
    private final Map<String, Integer> ENTRY = new HashMap<>();

    private ModConfigManager() {}

    public void registerConfig() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        //todo adapt other version use stonecutter
        //todo refact here
        AutoConfigClient.getGuiRegistry(ModConfig.class).registerPredicateProvider(
                (i18n, field, config, defaults, registry) -> {
                    ModConfig modConfig = (ModConfig) config;
                    IRollerMode[] allModes = RollerModeRegistry.getRollerModes().toArray(new IRollerMode[0]);

                    IRollerMode currentMode = RollerModeRegistry.getRollerModes().stream()
                            .filter(m -> m.getName().equals(modConfig.mode))
                            .findFirst()
                            .orElse(allModes[0]);

                    IRollerMode defaultMode = RollerModeRegistry.getRollerModes().stream()
                            .filter(m -> m.getName().equals(((ModConfig) defaults).mode))
                            .findFirst()
                            .orElse(allModes[0]);

                    return Collections.singletonList(
                            ConfigEntryBuilder.create()
                                    .startSelector(Component.translatable(i18n), allModes, currentMode)
                                    .setNameProvider(mode -> Component.literal(mode.getName()))
                                    .setDefaultValue(defaultMode)
                                    .setSaveConsumer(newMode -> modConfig.mode = newMode.getName())
                                    .build()
                    );
                },
                field -> field.getName().equals("mode") && field.getType() == String.class
        );
    }

    public ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public Screen getConfigScreen(Screen p) {
        //? if >= 1.21.11 {
        return AutoConfigClient.getConfigScreen(ModConfig.class, p).get();
        //?} else {
        /*return AutoConfig.getConfigScreen(ModConfig.class, p).get();
         *///?}
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

    public void registerEntry() {
        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

        IRollerMode[] allModes = RollerModeRegistry.getRollerModes().toArray(new IRollerMode[0]);

        // Find current mode object from saved name
        IRollerMode currentMode = RollerModeRegistry.getRollerModes().stream()
                .filter(m -> m.getName().equals(getConfig().mode))
                .findFirst()
                .orElse(allModes[0]);

        entryBuilder.startSelector(
                        Component.literal("Roller Mode"),
                        allModes,
                        currentMode
                )
                .setNameProvider(mode -> Component.literal(mode.getName()))
                .setDefaultValue(allModes[0])
                .setSaveConsumer(newMode -> getConfig().mode = newMode.getName()) // save name string
                .build();
    }

    public static ModConfigManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModConfigManager();
        }

        return INSTANCE;
    }
}

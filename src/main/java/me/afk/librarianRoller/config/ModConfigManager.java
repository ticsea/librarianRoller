package me.afk.librarianRoller.config;

import me.shedaniel.autoconfig.AutoConfig;
//? if >= 1.21.11 {
import me.shedaniel.autoconfig.AutoConfigClient;
//?}
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collections;

/**
 * Config holder: registers the AutoConfig, exposes the config and its GUI screen.
 * Parsing is delegated to {@link EnchantmentTargetParser}; business operations
 * (add hand/inventory) live in {@link ConfigService}.
 */
public class ModConfigManager {
    private final RollerModeRegistry rollerModeRegistry = new RollerModeRegistry();

    public void registerConfig() {
        rollerModeRegistry.register();
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        //~ if >= 1.21.11 'AutoConfig' -> 'AutoConfigClient' {
        AutoConfigClient.getGuiRegistry(ModConfig.class).registerPredicateProvider(
                (i18n, field, config, defaults, registry) -> {
                    ModConfig modConfig = (ModConfig) config;
                    IRollerMode[] allModes = rollerModeRegistry.getRollerModes().toArray(new IRollerMode[0]);

                    IRollerMode currentMode = rollerModeRegistry.getRollerModes().stream()
                            .filter(m -> m.getName().equals(modConfig.rollerMode))
                            .findFirst()
                            .orElse(allModes[0]);

                    IRollerMode defaultMode = rollerModeRegistry.getRollerModes().stream()
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

    public RollerModeRegistry getRollerModeRegistry() {
        return rollerModeRegistry;
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

}

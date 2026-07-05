package me.afk.librarianRoller.modmenuImp;

//? if FABRIC {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.afk.librarianRoller.config.ModConfigManager;

import static me.afk.librarianRoller.config.ModConfigManager.getConfigScreen;


public class ModMenuImp implements ModMenuApi  {
    /**
     * Used to construct a new config screen instance when your mod's
     * configuration button is selected on the mod menu screen. The
     * screen instance parameter is the active mod menu screen.
     *
     * @return A factory for constructing config screen instances.
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModConfigManager::getConfigScreen;
    }
}
//?}

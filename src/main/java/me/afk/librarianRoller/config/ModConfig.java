package me.afk.librarianRoller.config;

import me.afk.librarianRoller.LibrarianRoller;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Config(name = LibrarianRoller.MOD_ID)
public class ModConfig implements ConfigData {
//    public boolean isEnabled = false;
    public boolean autoBuy = false;
    public boolean preventAxeBreaking = true;
    public boolean legitMode = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public RollerType mode = RollerType.V1;

    @ConfigEntry.Gui.Tooltip
    public List<String> entry = new ArrayList<>();
}

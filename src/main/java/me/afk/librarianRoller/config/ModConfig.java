package me.afk.librarianRoller.config;

import com.mojang.blaze3d.platform.InputConstants;
import me.afk.librarianRoller.LibrarianRoller;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.impl.builders.KeyCodeBuilder;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Config(name = LibrarianRoller.MOD_ID)
public class ModConfig implements ConfigData {
//    public boolean isEnabled = false;
    //todo implement this
    public boolean autoBuy = false;
    public boolean preventAxeBreaking = true;
    //todo implement this
//    public boolean legitMode = true;

    //todo implement this
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
//    public ModeVType modeVType = ModeVType.SINGLE;

    public String rollerMode = "V1";

    @ConfigEntry.Gui.Tooltip
    public List<String> entry = new ArrayList<>(List.of("minecraft:mending 1", "minecraft:silk_touch 1"));
}

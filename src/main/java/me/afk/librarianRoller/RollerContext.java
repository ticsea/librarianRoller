package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.config.ModConfigManager;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class RollerContext {
    private static RollerContext INSTANCE;

    private final Minecraft minecraft;
    private final ModConfigManager modConfigManager;
    private final ModConfig modConfig;
    private boolean isEnabled = false;
    private int timeToBuy;
    private int pairIndex = 0;
    private List<VillagerAndLectern> list = new ArrayList<>();
    private IRollerPhase rollerPhase;

    private RollerContext() {
        this.rollerPhase = RollerPhaseInteract.INSTANCE;
        this.minecraft = Minecraft.getInstance();
        this.modConfigManager = ModConfigManager.getInstance();
        this.modConfig = modConfigManager.getConfig();
    }

    public int getTimeToBuy() {
        return timeToBuy;
    }

    public void setTimeToBuy(int timeToBuy) {
        this.timeToBuy = timeToBuy;
    }

    public void toggle() {
        rollerPhase.toggle(this);
    }

    public void doAction() {
        rollerPhase.doAction(this);
    }

    public void stop() {
        rollerPhase.stop(this);
    }

    public Minecraft getMinecraft() {
        return minecraft;
    }

    public ModConfigManager getModConfigManager() {
        return modConfigManager;
    }

    public ModConfig getModConfig() {
        return modConfig;
    }

    public boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getPairIndex() {
        return pairIndex;
    }

    public void setPairIndex(int pairIndex) {
        this.pairIndex = pairIndex;
    }

    public List<VillagerAndLectern> getList() {
        return list;
    }

    public void setList(List<VillagerAndLectern> list) {
        this.list = list;
    }

    public void setRollerPhase(IRollerPhase rollerPhase) {
        this.rollerPhase = rollerPhase;
    }

    public static RollerContext getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RollerContext();
        }

        return INSTANCE;
    }
}

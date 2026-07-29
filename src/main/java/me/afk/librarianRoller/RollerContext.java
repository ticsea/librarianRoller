package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.dataModel.EnchantBook;
import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class RollerContext {
    public static RollerContext INSTANCE = new RollerContext();

    private final MerchantPacketManager merchantPacketManager = MerchantPacketManager.INSTANCE;
    private final ModConfigManager modConfigManager;
    private boolean isEnabled = false;
    private int pairIndex = 0;
    private int merchantScreenId;
    private List<VillagerAndLectern> list = new ArrayList<>();
    private IRollerPhase rollerPhase;
    private EnchantBook enchantBook;

    public MerchantPacketManager getMerchantPacketManager() {
        return merchantPacketManager;
    }

    public int getMerchantScreenId() {
        return merchantScreenId;
    }

    public void setMerchantScreenId(int merchantScreenId) {
        this.merchantScreenId = merchantScreenId;
    }

    public IRollerPhase getRollerPhase() {
        return rollerPhase;
    }

    public EnchantBook getEnchantBook() {
        return enchantBook;
    }

    public void setEnchantBook(EnchantBook enchantBook) {
        this.enchantBook = enchantBook;
    }

    private RollerContext() {
        this.rollerPhase = RollerPhaseInteract.INSTANCE;
        this.modConfigManager = ModConfigManager.INSTANCE;
    }

    public void reset() {
        this.isEnabled = false;
        this.pairIndex = 0;
        this.merchantScreenId = -1;
        this.enchantBook = null;
        this.list.clear();
        this.rollerPhase = RollerPhaseInteract.INSTANCE;
        this.merchantPacketManager.reset();
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
        return Minecraft.getInstance();
    }

    public ModConfigManager getModConfigManager() {
        return modConfigManager;
    }

    public ModConfig getModConfig() {
        return modConfigManager.getConfig();
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
        this.list = new ArrayList<>(list);;
    }

    public void setRollerPhase(IRollerPhase rollerPhase) {
        this.rollerPhase = rollerPhase;
    }
}

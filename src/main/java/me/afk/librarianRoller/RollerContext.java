package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.dataModel.OfferData;
import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import me.afk.librarianRoller.utils.MessageUtils;
import me.afk.librarianRoller.utils.villagerAndLectern.IRollerMode;
import me.afk.librarianRoller.utils.villagerAndLectern.RollerModeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Context of the roller state machine (State Pattern).
 * Holds the shared state, injected dependencies, and the current phase.
 * Implements {@link RollerState} (read-only state for phases) and
 * {@link RollerTransitions} (transitions/dependencies for phases).
 * Local per-phase state (pickup wait, place failures) lives in the phases themselves.
 */
public class RollerContext implements RollerState, RollerTransitions {
    // P0: maximum consecutive villager failures before the roller stops entirely.
    public static final int MAX_CONSECUTIVE_FAILURES = 3;

    // Injected dependencies.
    private final MerchantPacketManager merchantPacketManager;
    private final ModConfigManager modConfigManager;
    // Cross-thread intent flags (Mixin communication).
    private final ScreenIntent screenIntent = new ScreenIntent();

    // Phase instances, constructed with injected dependencies.
    private final RollerPhaseInteract interact;
    private final RollerPhaseParse parse;
    private final RollerPhaseBreak breakPhase;
    private final RollerPhasePlace place;
    private final RollerPhaseBuy buy;

    // State-machine state.
    private boolean isEnabled = false;
    private int pairIndex = 0;
    private int merchantScreenId = -1;
    private List<VillagerAndLectern> list = new ArrayList<>();
    private IRollerPhase rollerPhase;
    private OfferData offerData;

    // P0: consecutive villager-pair failures (shared across phases).
    private int consecutiveFailures = 0;

    public RollerContext(MerchantPacketManager merchantPacketManager, ModConfigManager modConfigManager) {
        this.merchantPacketManager = merchantPacketManager;
        this.modConfigManager = modConfigManager;
        this.interact = new RollerPhaseInteract(this, this);
        this.parse = new RollerPhaseParse(this, this, merchantPacketManager, modConfigManager);
        this.breakPhase = new RollerPhaseBreak(this, this);
        this.place = new RollerPhasePlace(this, this);
        this.buy = new RollerPhaseBuy(this, this);
        this.rollerPhase = this.interact;
    }

    // --- RollerState (read-only) ---
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public int getPairIndex() {
        return pairIndex;
    }

    @Override
    public List<VillagerAndLectern> getList() {
        return list;
    }

    @Override
    public OfferData getEnchantBook() {
        return offerData;
    }

    @Override
    public int getMerchantScreenId() {
        return merchantScreenId;
    }

    // Public setter for the Mixin (network thread) to record the merchant container id.
    public void setMerchantScreenId(int merchantScreenId) {
        this.merchantScreenId = merchantScreenId;
    }

    @Override
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    // --- RollerTransitions ---
    @Override
    public void transitionTo(IRollerPhase next) {
        this.rollerPhase = next;
    }

    @Override
    public void stop() {
        reset();
        MessageUtils.print("afk.enchant_roller.info.turnoff");
    }

    @Override
    public void setEnchantBook(OfferData offerData) {
        this.offerData = offerData;
    }

    @Override
    public void advancePair() {
        if (list.isEmpty()) return;
        this.pairIndex = (this.pairIndex + 1) % list.size();
    }

    @Override
    public RollerPhaseInteract getInteract() {
        return interact;
    }

    @Override
    public RollerPhaseParse getParse() {
        return parse;
    }

    @Override
    public RollerPhaseBreak getBreakPhase() {
        return breakPhase;
    }

    @Override
    public RollerPhasePlace getPlace() {
        return place;
    }

    @Override
    public RollerPhaseBuy getBuy() {
        return buy;
    }

    @Override
    public MerchantPacketManager getMerchantPacketManager() {
        return merchantPacketManager;
    }

    @Override
    public ModConfigManager getModConfigManager() {
        return modConfigManager;
    }

    @Override
    public ModConfig getModConfig() {
        return modConfigManager.getConfig();
    }

    @Override
    public ScreenIntent getScreenIntent() {
        return screenIntent;
    }

    @Override
    public void onPairFailure() {
        this.consecutiveFailures++;
    }

    @Override
    public void onPairSuccess() {
        this.consecutiveFailures = 0;
    }

    @Override
    public boolean shouldStopAfterTooManyFailures() {
        return this.consecutiveFailures >= MAX_CONSECUTIVE_FAILURES;
    }

    // --- State-machine entry points (called by entrypoints) ---
    public void toggle() {
        if (isEnabled) {
            stop();
        } else {
            start();
        }
    }

    public void doAction() {
        rollerPhase.doAction();
    }

    private void start() {
        Minecraft minecraft = getMinecraft();
        if (minecraft == null) return;
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode interactionManager = minecraft.gameMode;

        if (player == null || interactionManager == null) {
            stop();
            return;
        }

        ModConfig modConfig = getModConfig();
        List<VillagerAndLectern> foundVillagers = getFoundVillagers(modConfig);

        if (foundVillagers.isEmpty()) {
            stop();
            MessageUtils.throwError("afk.enchant_roller.error.not_found_villager_or_lectern", Component.literal(modConfig.rollerMode));
            return;
        }

        if (modConfig.autoBuy) {
            if (!hasSufficientResources(player)) {
                stop();
                MessageUtils.throwError("afk.enchant_roller.error.not_enough_emerald_or_book");
                return;
            }
        }

        modConfigManager.setEntry();
        this.list = new ArrayList<>(foundVillagers);
        this.pairIndex = 0;
        this.consecutiveFailures = 0;
        this.isEnabled = true;
        this.rollerPhase = this.interact;
        // Reset local per-phase state (pickup ticks, place failures) before the new run.
        this.breakPhase.onReset();
        this.place.onReset();

        MessageUtils.print("afk.enchant_roller.info.turnon");
    }

    private static @NotNull List<VillagerAndLectern> getFoundVillagers(ModConfig modConfig) {
        return RollerModeRegistry.getRollerModes().stream()
                .filter(m -> m.getName().equals(modConfig.rollerMode))
                .findFirst()
                .map(IRollerMode::find)
                .orElse(List.of());
    }

    private boolean hasSufficientResources(LocalPlayer player) {
        int emeraldCount = player.getInventory().countItem(Items.EMERALD);
        int bookCount = player.getInventory().countItem(Items.BOOK);
        return emeraldCount >= 64 && bookCount >= 1;
    }

    public void reset() {
        this.isEnabled = false;
        this.pairIndex = 0;
        this.merchantScreenId = -1;
        this.offerData = null;
        this.list.clear();
        this.rollerPhase = this.interact;
        this.merchantPacketManager.reset();
        this.screenIntent.reset();
        this.consecutiveFailures = 0;
        // Reset local per-phase state (pickup ticks, place failures).
        this.breakPhase.onReset();
        this.place.onReset();
    }
}

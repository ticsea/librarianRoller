package me.afk.librarianRoller;

import me.afk.librarianRoller.config.EnchantmentTargetParser;
import me.afk.librarianRoller.config.IRollerMode;
import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.config.RollerModeRegistry;
import me.afk.librarianRoller.dataModel.Librarians;
import me.afk.librarianRoller.dataModel.MerchantOfferSnapshot;
import me.afk.librarianRoller.utils.MessageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context of the roller state machine (classic State Pattern, event-driven).
 * <p>
 * Owns ALL mutable state - the phases are stateless pure executors that read
 * {@link RollerState} and emit {@link RollerEvent}s. This context translates each
 * (state, event) pair through a data-driven transition table
 * ({@link EnumMap}<{@link RollerPhase}, {@link EnumMap}<{@link RollerEvent.Type}, {@link Transition}>>):
 * each row binds an optional side effect (pair counters, enchant-book storage) with
 * the target phase. Adding a new state/event/transition is a single table row.
 */
public class RollerContext implements RollerState, RollerTransitions {
    private static final Logger LOGGER = LoggerFactory.getLogger("RollerContext");

    // P0: maximum consecutive villager failures before the roller stops entirely.
    public static final int MAX_CONSECUTIVE_FAILURES = 3;

    // Injected dependencies.
    private final MerchantPacketManager merchantPacketManager;
    private final ModConfigManager modConfigManager;
    // Cross-thread intent flags (Mixin communication).
    private final ScreenIntent screenIntent = new ScreenIntent();

    // Stateless phase executors, constructed with narrow interfaces.
    private final RollerPhaseInteract interact;
    private final RollerPhaseParse parse;
    private final RollerPhaseBreak breakPhase;
    private final RollerPhasePlace place;
    private final RollerPhaseBuy buy;

    // Data-driven transition table: (current state, event) -> (side effect, next state).
    private final EnumMap<RollerPhase, EnumMap<RollerEvent.Type, Transition>> transitionTable;

    // State-machine state.
    private boolean isEnabled = false;
    private int pairIndex = 0;
    private int merchantScreenId = -1;
    private List<Librarians> list = new ArrayList<>();
    private RollerPhase currentPhase = RollerPhase.INTERACT;
    private MerchantOfferSnapshot.SingleTradeEntry offerData;
    // Parsed enchantment target map (cached at roller start).
    private final Map<String, Integer> entryCache = new HashMap<>();

    // P0: consecutive villager-pair failures (shared across phases).
    private int consecutiveFailures = 0;
    // Tick-counting state, owned here so the phases stay stateless.
    private int pickupWaitTicks = 0;
    private int placeFailures = 0;

    public RollerContext(MerchantPacketManager merchantPacketManager, ModConfigManager modConfigManager) {
        this.merchantPacketManager = merchantPacketManager;
        this.modConfigManager = modConfigManager;
        this.interact = new RollerPhaseInteract(this, this);
        this.parse = new RollerPhaseParse(this, this);
        this.breakPhase = new RollerPhaseBreak(this, this);
        this.place = new RollerPhasePlace(this, this);
        this.buy = new RollerPhaseBuy(this, this);
        this.transitionTable = buildTransitionTable();
    }

    // ------------------------------------------------------------------
    // Transition table (data-driven). One row per (state, event).
    // Side effects run BEFORE the next phase is resolved (R2 ordering).
    // ------------------------------------------------------------------
    private EnumMap<RollerPhase, EnumMap<RollerEvent.Type, Transition>> buildTransitionTable() {
        EnumMap<RollerPhase, EnumMap<RollerEvent.Type, Transition>> table = new EnumMap<>(RollerPhase.class);

        table.put(RollerPhase.INTERACT, row(
                event(RollerEvent.Type.WAITING, Transition.to(RollerPhase.INTERACT)),
                event(RollerEvent.Type.INTERACT_FAILED, Transition.to(RollerPhase.INTERACT)),
                event(RollerEvent.Type.INTERACT_SUCCESS, Transition.to(RollerPhase.PARSE)),
                event(RollerEvent.Type.LECTERN_MISSING, Transition.to(RollerPhase.PLACE)),
                event(RollerEvent.Type.FATAL, Transition.to(RollerPhase.STOP))
        ));
        table.put(RollerPhase.PARSE, row(
                event(RollerEvent.Type.WAITING, Transition.to(RollerPhase.PARSE)),
                // TRADE_MATCHED handled in #handleEvent (payload); the table only
                // decides the destination (autoBuy -> BUY, otherwise STOP).
                event(RollerEvent.Type.TRADE_MATCHED, Transition.to(ctx -> ctx.getModConfig().autoBuy ? RollerPhase.BUY : RollerPhase.STOP)),
                event(RollerEvent.Type.NO_TRADE_MATCH, Transition.to(RollerPhase.BREAK)),
                event(RollerEvent.Type.FATAL, Transition.to(RollerPhase.STOP))
        ));
        table.put(RollerPhase.BREAK, row(
                event(RollerEvent.Type.BREAKING, Transition.to(RollerPhase.BREAK)),
                event(RollerEvent.Type.WAITING, Transition.to(RollerPhase.BREAK, RollerContext::incrementPickupWait)),
                event(RollerEvent.Type.PICKUP_COMPLETE, Transition.to(RollerPhase.PLACE, RollerContext::resetPickupWait)),
                event(RollerEvent.Type.PICKUP_TIMEOUT, Transition.to(
                        ctx -> ctx.shouldStopAfterTooManyFailures() ? RollerPhase.STOP : RollerPhase.INTERACT,
                        RollerContext::failPairPickup
                )),
                event(RollerEvent.Type.FATAL, Transition.to(RollerPhase.STOP))
        ));
        table.put(RollerPhase.PLACE, row(
                event(RollerEvent.Type.WAITING, Transition.to(RollerPhase.PLACE, RollerContext::incrementPickupWait)),
                event(RollerEvent.Type.PLACE_RETRY, Transition.to(RollerPhase.PLACE, RollerContext::incrementPlaceFailures)),
                event(RollerEvent.Type.PLACE_FAILED, Transition.to(
                        ctx -> ctx.shouldStopAfterTooManyFailures() ? RollerPhase.STOP : RollerPhase.INTERACT,
                        RollerContext::failPairPlace
                )),
                event(RollerEvent.Type.PLACE_SUCCESS, Transition.to(RollerPhase.INTERACT, RollerContext::successPair)),
                event(RollerEvent.Type.FATAL, Transition.to(RollerPhase.STOP))
        ));
        table.put(RollerPhase.BUY, row(
                event(RollerEvent.Type.WAITING, Transition.to(RollerPhase.BUY)),
                event(RollerEvent.Type.BUY_COMPLETE, Transition.to(RollerPhase.STOP)),
                event(RollerEvent.Type.FATAL, Transition.to(RollerPhase.STOP))
        ));

        return table;
    }

    private static EnumMap<RollerEvent.Type, Transition> row(Map.Entry<RollerEvent.Type, Transition>... entries) {
        EnumMap<RollerEvent.Type, Transition> map = new EnumMap<>(RollerEvent.Type.class);
        for (var entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private static Map.Entry<RollerEvent.Type, Transition> event(RollerEvent.Type type, Transition transition) {
        return Map.entry(type, transition);
    }

    // --- Side effects bound to transition rows ---
    private void incrementPickupWait() {
        this.pickupWaitTicks++;
    }

    private void resetPickupWait() {
        this.pickupWaitTicks = 0;
    }

    private void incrementPlaceFailures() {
        this.placeFailures++;
    }

    /**
     * P0: record a pickup failure, skip the current villager and eventually give up.
     */
    private void failPairPickup() {
        failPair("afk.enchant_roller.error.lectern_pickup_failed");
    }

    /**
     * P0: record a place failure, skip the current villager and eventually give up.
     */
    private void failPairPlace() {
        failPair("afk.enchant_roller.error.lectern_place_failed");
    }

    private void failPair(String errorKey) {
        this.pickupWaitTicks = 0;
        this.placeFailures = 0;
        this.onPairFailure();
        if (this.shouldStopAfterTooManyFailures()) {
            MessageUtils.throwError(errorKey);
        } else {
            // Skip this pair and move on to the next villager.
            this.advancePair();
        }
    }

    /** Full-cycle success: reset failure counters and advance to the next villager. */
    private void successPair() {
        this.pickupWaitTicks = 0;
        this.placeFailures = 0;
        this.onPairSuccess();
        this.advancePair();
    }

    /**
     * TRADE_MATCHED carries the matched trade payload, so its data side effects
     * (store the book + print the reward) run here at dispatch time. The transition
     * table still decides where to go next (autoBuy -> BUY, otherwise STOP).
     */
    private void onTradeMatched(MerchantOfferSnapshot.SingleTradeEntry match) {
        this.setEnchantBook(match);
        match.enchantments().forEach(enchantment -> {
            MessageUtils.printReward(enchantment.name(), enchantment.level(), match.cost());
        });
    }

    // --- Event dispatch (the only place a transition happens) ---
    public void handleEvent(RollerEvent event) {
        if (event == null || event.type() == null) {
            stop();
            return;
        }
        // Payload-carrying event: process its data side effects before the table lookup.
        if (event.type() == RollerEvent.Type.TRADE_MATCHED && event.matchedTrade() != null) {
            onTradeMatched(event.matchedTrade());
        }

        Transition transition = lookup(this.currentPhase, event.type());
        if (transition == null) {
            // R6 default fallback: an event we did not expect in this state is a bug -
            // fail closed instead of silently stalling.
            LOGGER.error("Unhandled event {} in phase {}", event.type(), this.currentPhase);
            stop();
            return;
        }
        if (transition.sideEffect() != null) {
            transition.sideEffect().accept(this);
        }
        RollerPhase next = transition.next().apply(this);
        transitionTo(next);
    }

    private Transition lookup(RollerPhase phase, RollerEvent.Type type) {
        var byEvent = this.transitionTable.get(phase);
        if (byEvent == null) return null;
        return byEvent.get(type);
    }

    private void transitionTo(RollerPhase next) {
        if (next == RollerPhase.STOP) {
            stop();
        } else {
            this.currentPhase = next;
        }
    }

    private IRollerPhase executorOf(RollerPhase phase) {
        return switch (phase) {
            case INTERACT -> interact;
            case PARSE -> parse;
            case BREAK -> breakPhase;
            case PLACE -> place;
            case BUY -> buy;
            case STOP -> null; // unreachable: STOP is never executed
        };
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
    public List<Librarians> getList() {
        return list;
    }

    @Override
    public MerchantOfferSnapshot.SingleTradeEntry getEnchantBook() {
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

    @Override
    public Map<String, Integer> getEntry() {
        return entryCache;
    }

    @Override
    public int getPickupWaitTicks() {
        return pickupWaitTicks;
    }

    @Override
    public int getPlaceFailures() {
        return placeFailures;
    }

    /**
     * Re-parses the config's entry list into the cache. Called once at roller start.
     */
    private void setEntry() {
        entryCache.clear();
        entryCache.putAll(EnchantmentTargetParser.parse(getModConfig().entry));
    }

    // --- RollerTransitions ---
    @Override
    public void stop() {
        reset();
        MessageUtils.print("afk.enchant_roller.info.turnoff");
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

    // --- Internal mutable state (used by transition table side effects) ---
    public void setEnchantBook(MerchantOfferSnapshot.SingleTradeEntry offerData) {
        this.offerData = offerData;
    }

    public void advancePair() {
        if (list.isEmpty()) return;
        this.pairIndex = (this.pairIndex + 1) % list.size();
    }

    public void onPairFailure() {
        this.consecutiveFailures++;
    }

    public void onPairSuccess() {
        this.consecutiveFailures = 0;
    }

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
        if (!isEnabled) return;
        IRollerPhase executor = executorOf(currentPhase);
        if (executor == null) {
            stop();
            return;
        }
        RollerEvent event = executor.doAction();
        handleEvent(event);
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
        List<Librarians> foundVillagers = getFoundVillagers(modConfig, modConfigManager.getRollerModeRegistry());

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

        setEntry();
        this.list = new ArrayList<>(foundVillagers);
        this.pairIndex = 0;
        this.consecutiveFailures = 0;
        this.pickupWaitTicks = 0;
        this.placeFailures = 0;
        this.isEnabled = true;
        this.currentPhase = RollerPhase.INTERACT;

        MessageUtils.print("afk.enchant_roller.info.turnon");
    }

    private static @NotNull List<Librarians> getFoundVillagers(ModConfig modConfig, RollerModeRegistry rollerModeRegistry) {
        return rollerModeRegistry.getRollerModes().stream()
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
        this.entryCache.clear();
        this.currentPhase = RollerPhase.INTERACT;
        this.pickupWaitTicks = 0;
        this.placeFailures = 0;
        this.consecutiveFailures = 0;
        this.merchantPacketManager.reset();
        this.screenIntent.reset();
    }
}
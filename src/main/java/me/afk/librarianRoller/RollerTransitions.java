package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.dataModel.OfferData;

/**
 * State-transition and dependency access for the state machine phases.
 * Phases depend on this narrow interface (not the full RollerContext) so they can be
 * unit-tested with a mock implementation.
 */
public interface RollerTransitions {
    // --- State transitions ---
    void transitionTo(IRollerPhase next);

    void stop();

    // --- Mutable state (set by phases) ---
    void setEnchantBook(OfferData offerData);

    void advancePair();

    // --- Phase accessors (for transitions) ---
    RollerPhaseInteract getInteract();

    RollerPhaseParse getParse();

    RollerPhaseBreak getBreakPhase();

    RollerPhasePlace getPlace();

    RollerPhaseBuy getBuy();

    // --- Dependency access ---
    MerchantPacketManager getMerchantPacketManager();

    ModConfigManager getModConfigManager();

    ModConfig getModConfig();

    ScreenIntent getScreenIntent();

    // --- Failure counters (P0) ---
    void onPairFailure();

    void onPairSuccess();

    boolean shouldStopAfterTooManyFailures();
}
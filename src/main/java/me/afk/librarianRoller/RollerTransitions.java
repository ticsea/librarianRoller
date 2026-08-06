package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.config.ModConfigManager;

/**
 * Dependency access for the state machine phases and their helper utilities.
 * <p>
 * In the event-driven design the phases do NOT perform transitions - they only
 * emit {@link RollerEvent}s, which the {@link RollerContext} translates via the
 * transition table. This interface therefore exposes no transition methods;
 * it is limited to the dependencies a phase or tool needs to interact with the
 * game world, plus {@link #stop()} which is used by stateless helpers
 * (e.g. {@code PlayerInventoryUtils.preventAxeBreaking}).
 */
public interface RollerTransitions {
    /**
     * Stops the roller. Reserved for stateless helper utilities that detect a
     * fatal condition before the phase has a chance to emit an event.
     * Phases themselves should emit {@link RollerEvent.Type#FATAL} instead.
     */
    void stop();

    // --- Dependency access ---
    MerchantPacketManager getMerchantPacketManager();

    ModConfigManager getModConfigManager();

    ModConfig getModConfig();

    ScreenIntent getScreenIntent();
}
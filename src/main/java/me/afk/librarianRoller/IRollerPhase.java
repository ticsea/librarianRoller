package me.afk.librarianRoller;

/**
 * State interface for the roller state machine (State Pattern).
 * Each concrete phase implements {@link #doAction()} and performs state transitions
 * through the injected {@link RollerTransitions}. Phases are constructed with their
 * required dependencies (narrow interfaces), so they can be unit-tested with mocks.
 */
public interface IRollerPhase {
    /**
     * Performs one tick of this phase's behavior. Uses the injected {@link RollerState}
     * (read-only state) and {@link RollerTransitions} (transitions/dependencies).
     */
    void doAction();

    /**
     * Called by the context when the roller is started/reset. Phases with local state
     * (e.g. pickup-wait ticks, place-failure counters) clear it here so a stop->start
     * cycle does not leak stale counters into the next run.
     */
    default void onReset() {
    }
}

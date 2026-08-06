package me.afk.librarianRoller;

/**
 * State interface for the roller state machine (State Pattern).
 * <p>
 * A phase is a <b>stateless pure executor</b>: it reads the injected {@link RollerState}
 * (and, only where needed, the {@link RollerTransitions} dependency accessor), performs
 * one tick of game interaction, and returns a {@link RollerEvent}. It never mutates
 * state and never performs transitions - the {@link RollerContext} owns all mutable
 * data and translates events into transitions via the transition table.
 */
public interface IRollerPhase {
    /**
     * Performs one tick of this phase's behavior and returns the resulting event.
     *
     * @return the event describing what happened; the context maps it to a transition.
     */
    RollerEvent doAction();
}
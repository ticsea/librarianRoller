package me.afk.librarianRoller;

/**
 * The distinct states of the roller state machine.
 * <p>
 * A pure enum - holds no data. The context maps each phase enum to its
 * actual (stateless) phase executor via {@link RollerContext}.
 * <p>
 * {@link #STOP} is a sentinel state meaning "the machine is off"; it is never
 * passed to an executor, only used as a transition target.
 */
public enum RollerPhase {
    INTERACT,
    PARSE,
    BREAK,
    PLACE,
    BUY,
    STOP
}
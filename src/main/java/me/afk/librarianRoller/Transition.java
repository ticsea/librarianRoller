package me.afk.librarianRoller;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * One row of the transition table: what to do when an event fires in a state.
 * <p>
 * Pure data record. {@link #sideEffect()} runs first (mutating context state such
 * as pair counters / enchant book), then {@link #next()} resolves the target phase.
 * {@code next} is a {@link Function} rather than a constant enum so that dynamic
 * decisions (e.g. autoBuy -> BUY vs STOP) can be expressed declaratively in the table.
 *
 * @param next        resolves the target phase after the side effect has run.
 * @param sideEffect  optional context mutation tied to this transition (may be null).
 */
public record Transition(
        Function<RollerContext, RollerPhase> next,
        Consumer<RollerContext> sideEffect
) {
    public static Transition to(RollerPhase phase) {
        return new Transition(ctx -> phase, null);
    }

    public static Transition to(RollerPhase phase, Consumer<RollerContext> sideEffect) {
        return new Transition(ctx -> phase, sideEffect);
    }

    public static Transition to(Function<RollerContext, RollerPhase> next) {
        return new Transition(next, null);
    }

    public static Transition to(Function<RollerContext, RollerPhase> next, Consumer<RollerContext> sideEffect) {
        return new Transition(next, sideEffect);
    }
}
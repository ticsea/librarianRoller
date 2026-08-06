package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.MerchantOfferSnapshot;

/**
 * Event emitted by a phase's {@link IRollerPhase#doAction()}.
 * <p>
 * Pure data - carries no logic. A record wrapping the {@link Type} enum so that
 * {@link Type#TRADE_MATCHED} can also carry the matched trade entry to the context
 * (which stores it via {@code setEnchantBook} and prints the reward).
 * <p>
 * The transition table is keyed by {@link #type()}.
 */
public record RollerEvent(Type type, MerchantOfferSnapshot.SingleTradeEntry matchedTrade) {

    public enum Type {
        // Common
        WAITING,        // Nothing to do this tick - stay in the current phase.
        FATAL,          // Unrecoverable error - stop the roller.

        // INTERACT
        INTERACT_SUCCESS,
        INTERACT_FAILED,
        LECTERN_MISSING,

        // PARSE
        TRADE_MATCHED,  // Carries the matched trade as payload.
        NO_TRADE_MATCH,

        // BREAK
        BREAKING,       // Still destroying the lectern - do not count pickup wait.
        PICKUP_COMPLETE,
        PICKUP_TIMEOUT,

        // PLACE
        PLACE_SUCCESS,
        PLACE_RETRY,    // Placement failed once - stay and retry.
        PLACE_FAILED,   // Placement retry budget exhausted - skip this pair.

        // BUY
        BUY_COMPLETE
    }

    public static RollerEvent of(Type type) {
        return new RollerEvent(type, null);
    }

    public static RollerEvent tradeMatched(MerchantOfferSnapshot.SingleTradeEntry matchedTrade) {
        return new RollerEvent(Type.TRADE_MATCHED, matchedTrade);
    }
}
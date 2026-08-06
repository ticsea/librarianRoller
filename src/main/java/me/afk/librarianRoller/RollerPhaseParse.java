package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.MerchantOfferSnapshot;

/**
 * PARSE phase - a stateless pure executor.
 * <p>
 * Consumes the latest merchant trade snapshot and decides whether it matches the
 * configured enchantment targets. Emits {@link RollerEvent.Type#TRADE_MATCHED} with
 * the matched trade as payload, {@link RollerEvent.Type#NO_TRADE_MATCH}, or
 * {@link RollerEvent.Type#WAITING} when no packet has arrived yet (stay in PARSE).
 * <p>
 * The autoBuy policy and the reward printing are side effects owned by the
 * {@link RollerContext} transition table, not by this phase.
 */
public class RollerPhaseParse implements IRollerPhase {
    private final RollerState state;
    private final RollerTransitions transitions;

    public RollerPhaseParse(RollerState state, RollerTransitions transitions) {
        this.state = state;
        this.transitions = transitions;
    }

    @Override
    public RollerEvent doAction() {
        if (!state.isEnabled()) return RollerEvent.of(RollerEvent.Type.WAITING);

        MerchantOfferSnapshot latestTradeSnapshot = transitions.getMerchantPacketManager().tryConsumePendingTradeData();
        if (latestTradeSnapshot == null) {
            // No trade packet has arrived yet - keep waiting in PARSE.
            return RollerEvent.of(RollerEvent.Type.WAITING);
        }

        var match = latestTradeSnapshot.findMatch(state.getEntry());
        if (match != null) {
            return RollerEvent.tradeMatched(match);
        }
        return RollerEvent.of(RollerEvent.Type.NO_TRADE_MATCH);
    }
}
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
    // Bounded waiting: if the merchant-offers packet never arrives after a successful
    // interaction (villager lost its profession on the server, dropped/intercepted
    // packet), bail out to BREAK instead of stalling in PARSE forever. Matches the
    // pickup/place timeout of the BREAK and PLACE phases (2 seconds).
    private static final int MAX_PARSE_WAIT_TICKS = 40;

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
            // No trade packet has arrived yet - keep waiting in PARSE, but only for a
            // bounded number of ticks. The context increments the counter on WAITING
            // (entering PARSE always resets it), so a packet that never arrives cannot
            // dead-lock the machine.
            if (state.getParseWaitTicks() + 1 >= MAX_PARSE_WAIT_TICKS) {
                // Diagnostic: firing the timeout means the writer never published a
                // snapshot. Either the packet never reached acceptMerchantPacket, or
                // it was consumed/cleared before PARSE read it.
                org.slf4j.LoggerFactory.getLogger("RollerPhaseParse")
                        .warn("[Roller] PARSE_TIMEOUT after {} ticks: pendingTradeData stayed null. " +
                                "If 'acceptMerchantPacket -> publishing' never appears in the log, the packet is not reaching the writer.",
                                state.getParseWaitTicks());
                return RollerEvent.of(RollerEvent.Type.PARSE_TIMEOUT);
            }
            return RollerEvent.of(RollerEvent.Type.WAITING);
        }

        var match = latestTradeSnapshot.findMatch(state.getEntry());
        // Diagnostic: log what we actually parsed and whether it matched the targets.
        org.slf4j.LoggerFactory.getLogger("RollerPhaseParse")
                .info("[Roller] PARSE consumed {} offers, entry targets {}, match={}",
                        latestTradeSnapshot.offers().size(), state.getEntry().size(), match != null);
        if (match != null) {
            return RollerEvent.tradeMatched(match);
        }
        return RollerEvent.of(RollerEvent.Type.NO_TRADE_MATCH);
    }
}
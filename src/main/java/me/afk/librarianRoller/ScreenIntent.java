package me.afk.librarianRoller;

/**
 * Cross-thread intent flags used by the Mixin to decide whether to cancel or let through
 * an OpenScreen packet. Written on the main thread (state machine), read on the network
 * thread (Mixin) -> volatile.
 * <p>
 * Single-enum representation: the two booleans of the old design were mutually exclusive
 * by nature (an automated interaction and an autoBuy re-interaction can never be in
 * flight at the same time), so a single {@link Mode} removes the "set A, forgot to clear B"
 * class of bugs.
 * <p>
 * An {@code epoch} token is bumped on every set/consume. The Mixin validates the epoch
 * when consuming: if it does not match the value it set, the intent is stale (e.g. an
 * interact failed and the phase cleared the flag, but a spurious OpenScreen still arrives)
 * and is ignored instead of being mis-consumed.
 */
public class ScreenIntent {
    public enum Mode {
        NONE,               // No automated interaction in flight.
        CANCEL,             // Automated interaction: Mixin cancels the resulting OpenScreen.
        ALLOW_AND_RECORD    // autoBuy re-interaction: Mixin lets the MerchantScreen through and records its container id.
    }

    // The current intent mode, read/written across threads.
    private volatile Mode mode = Mode.NONE;
    // Monotonic token. A consume is only honored if the token is still the one that was set.
    private volatile long epoch = 0;

    /** Sets an intent. Returns the epoch that must be presented at consume time. */
    public synchronized long set(Mode mode) {
        this.mode = mode;
        return ++this.epoch;
    }

    /**
     * Consume-and-clear if the intent is still fresh.
     *
     * @param expectedEpoch the epoch returned by {@link #set(Mode)}.
     * @return the current mode if {@code expectedEpoch == this.epoch}, otherwise {@link Mode#NONE}
     *         (stale intent - e.g. the phase already cleared it after a failed interaction).
     */
    public synchronized Mode consume(long expectedEpoch) {
        if (expectedEpoch != this.epoch) {
            // Stale intent (the setter already cleared it). Do not touch the current mode.
            return Mode.NONE;
        }
        Mode current = this.mode;
        this.mode = Mode.NONE;
        this.epoch++;
        return current;
    }

    public synchronized Mode getMode() {
        return this.mode;
    }

    public synchronized void reset() {
        this.mode = Mode.NONE;
        this.epoch++;
    }
}
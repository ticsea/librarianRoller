package me.afk.librarianRoller;

/**
 * Cross-thread intent flags used by the Mixin to decide whether to cancel or let through
 * an OpenScreen packet. Written on the main thread (state machine), read on the network
 * thread (Mixin) -> volatile.
 */
public class ScreenIntent {
    // Set right before an automated villager interaction: the Mixin cancels the resulting OpenScreen.
    private volatile boolean awaitingMerchantScreen = false;
    // Set right before an autoBuy re-interaction: the Mixin lets the MerchantScreen through
    // and records its container id.
    private volatile boolean buyScreenPending = false;

    public void setAwaitingMerchantScreen(boolean awaiting) {
        this.awaitingMerchantScreen = awaiting;
    }

    // Consume-and-clear: returns true if an automated interaction is pending.
    public boolean consumeAwaitingMerchantScreen() {
        boolean pending = this.awaitingMerchantScreen;
        this.awaitingMerchantScreen = false;
        return pending;
    }

    public void setBuyScreenPending(boolean pending) {
        this.buyScreenPending = pending;
    }

    // Consume-and-clear: returns true if an autoBuy re-interaction is pending.
    public boolean consumeBuyScreenPending() {
        boolean pending = this.buyScreenPending;
        this.buyScreenPending = false;
        return pending;
    }

    public void reset() {
        this.awaitingMerchantScreen = false;
        this.buyScreenPending = false;
    }
}
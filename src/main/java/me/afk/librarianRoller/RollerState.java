package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.MerchantOfferSnapshot;
import me.afk.librarianRoller.dataModel.Librarians;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Map;

/**
 * Read-only view of the roller's shared state, exposed to the state machine phases.
 * <p>
 * Phases are stateless pure executors: they read this interface, emit a
 * {@link RollerEvent}, and never mutate anything. All mutable data lives in the
 * {@link RollerContext} (the only state owner).
 */
public interface RollerState {
    boolean isEnabled();

    int getPairIndex();

    List<Librarians> getList();

    MerchantOfferSnapshot.SingleTradeEntry getEnchantBook();

    int getMerchantScreenId();

    Minecraft getMinecraft();

    /**
     * Parsed enchantment target map (cached at roller start).
     */
    Map<String, Integer> getEntry();

    // --- Tick-counting state (owned by the context, incremented on WAITING/retry events) ---

    /**
     * Ticks spent waiting for the lectern to reappear in the inventory after breaking.
     * Read by {@link RollerPhaseBreak}; incremented by the context.
     */
    int getPickupWaitTicks();

    /**
     * Consecutive failed place attempts for the current villager.
     * Read by {@link RollerPhasePlace}; incremented by the context.
     */
    int getPlaceFailures();
}
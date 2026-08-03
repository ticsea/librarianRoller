package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.OfferData;
import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Read-only view of the roller's shared state, exposed to the state machine phases.
 * Phases depend on this narrow interface (not the full RollerContext) so they can be
 * unit-tested with a mock implementation.
 */
public interface RollerState {
    boolean isEnabled();

    int getPairIndex();

    List<VillagerAndLectern> getList();

    OfferData getEnchantBook();

    int getMerchantScreenId();

    Minecraft getMinecraft();
}
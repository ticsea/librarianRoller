package me.afk.librarianRoller.dataModel;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;

/**
 * Immutable parsed merchant trade snapshot from ClientboundMerchantOffersPacket
 */
public record MerchantTradeData(
        List<SingleTradeEntry> offers
) {
    public record SingleTradeEntry(
            ItemStack costA,
            ItemStack costB,
            ItemStack result
    ) {
        public static SingleTradeEntry fromTradeOffer(MerchantOffer offer) {
            return new SingleTradeEntry(
                    offer.getCostA().copy(),
                    offer.getCostB().copy(),
                    offer.getResult().copy()
            );
        }
    }
}
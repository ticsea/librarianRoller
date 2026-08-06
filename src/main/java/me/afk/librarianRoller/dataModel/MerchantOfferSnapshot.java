package me.afk.librarianRoller.dataModel;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;

/**
 * Immutable parsed merchant trade snapshot from ClientboundMerchantOffersPacket.
 * <p>
 * Each {@link SingleTradeEntry} carries both the raw ItemStacks (costA, costB, result)
 * and, after enchantment parsing in {@code RollerPhaseParse.findMatch}, the parsed
 * enchantment metadata (name, level, index). This consolidates what was previously
 * split across {@code MerchantTradeData} and {@code OfferData} into a single type.
 */
public record MerchantTradeData(
        List<SingleTradeEntry> offers
) {
    public record SingleTradeEntry(
            ItemStack costA,
            ItemStack costB,
            ItemStack result,
            String name,
            int level,
            int index
    ) {
        /**
         * Creates a raw trade entry from a {@link MerchantOffer}.
         * The parsed fields (name, level, index) are left unset (null / 0 / -1)
         * and are populated later by {@code findMatch} when an enchantment match is found.
         */
        public static SingleTradeEntry fromTradeOffer(MerchantOffer offer) {
            return new SingleTradeEntry(
                    offer.getCostA().copy(),
                    offer.getCostB().copy(),
                    offer.getResult().copy(),
                    null,
                    0,
                    -1
            );
        }

        /**
         * Convenience accessor for the emerald cost count (costA stack size).
         * Replaces the former {@code OfferData.cost} field.
         */
        public int cost() {
            return costA.getCount();
        }
    }
}

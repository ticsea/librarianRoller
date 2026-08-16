package me.afk.librarianRoller.dataModel;

import me.afk.librarianRoller.utils.ItemEnchantmentParsingUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Immutable parsed merchant trade snapshot from ClientboundMerchantOffersPacket.
 * <p>
 * Each {@link SingleTradeEntry} carries both the raw ItemStacks (costA, costB, result)
 * and, after enchantment parsing in {@code RollerPhaseParse.findMatch}, the parsed
 * enchantment metadata (name, level, index). This consolidates what was previously
 * split across {@code MerchantTradeData} and {@code OfferData} into a single type.
 */
public record MerchantOfferSnapshot(
        List<SingleTradeEntry> offers
) {
    private final static Logger LOGGER = LoggerFactory.getLogger("MerchantOfferSnapshot");

    public MerchantOfferSnapshot.SingleTradeEntry findMatch(Map<String, Integer> targetEnchantments) {

        for (var object : offers) {
            List<Enchantment> enchantments = object.enchantments();

            for (var book : enchantments) {
                int lvl = book.level();
                String name = book.name();
                LOGGER.debug("THERE ARE ENCHENMENTS: {} {}", name, lvl);

                Integer requiredLevel = targetEnchantments.get(name);
                if (requiredLevel != null && lvl >= requiredLevel) {
                    return object;
                }
            }
        }

        return null;
    }

    public record SingleTradeEntry(
            int index,
            ItemStack costA,
            ItemStack costB,
            ItemStack result
    ) {
        /**
         * Creates a raw trade entry from a {@link MerchantOffer}.
         * The parsed fields (name, level, index) are left unset (null / 0 / -1)
         * and are populated later by {@code findMatch} when an enchantment match is found.
         */
        public static SingleTradeEntry fromOffer(int index, MerchantOffer offer) {
            return new SingleTradeEntry(
                    index,
                    offer.getCostA().copy(),
                    offer.getCostB().copy(),
                    offer.getResult().copy()
            );
        }

        /**
         * Convenience accessor for the emerald cost count (costA stack size).
         * Replaces the former {@code OfferData.cost} field.
         */
        public int cost() {
            return costA.getCount();
        }

        public List<Enchantment> enchantments() {
            return ItemEnchantmentParsingUtils.readStoredEnchantments(result);
        }
    }
}

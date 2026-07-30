package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.Enchantment;
import me.afk.librarianRoller.dataModel.OfferData;
import me.afk.librarianRoller.dataModel.MerchantTradeData;
import me.afk.librarianRoller.utils.EnchantedBookUtils;
import me.afk.librarianRoller.utils.MessageUtils;
        //? if >= 1.21.1 {
        //?} else >= 1.20.1 {

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
        //?}
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RollerPhaseParse implements IRollerPhase{
    public final Logger LOGGER = LoggerFactory.getLogger("RollerPhaseParse");
    public static final RollerPhaseParse INSTANCE = new RollerPhaseParse();

    /*static {
        MerchantPacketManager.INSTANCE.subscribeTradeUpdate(data -> {

        });
    }*/

    @Override
    public void doAction(RollerContext ctx) {
        if (!ctx.getEnabled()) return;

        MerchantTradeData latestTradeSnapshot = ctx.getMerchantPacketManager().tryConsumePendingTradeData();
        if (latestTradeSnapshot == null) return;
        var match = findMatch(latestTradeSnapshot.offers(), ctx.getModConfigManager().getEntry());

        if (match != null) {
            ctx.setEnchantBook(match);
            MessageUtils.printReward(match.name(), match.level(), match.cost());
            if (ctx.getModConfigManager().getConfig().autoBuy) {
                ctx.setRollerPhase(RollerPhaseBuy.INSTANCE);
            } else {
                ctx.stop();
            }
        } else {
            ctx.setRollerPhase(RollerPhaseBreak.INSTANCE);
        }
    }

    //? if >= 1.21.1 {
    /*private OfferData findMatch(List<MerchantTradeData.SingleTradeEntry> offers, Map<String, Integer> tradEntry) {
        //fixme some version like 1.20.1 use another way but datacomponents

        for (int i = 0; i < offers.size(); ++i) {
            MerchantTradeData.SingleTradeEntry singleTradeEntry = offers.get(i);
            ItemStack result = singleTradeEntry.result();
            List<Enchantment> enchantments = EnchantedBookUtils.readStoredEnchantments(result);

            for (var book : enchantments) {
                int lvl = book.level();
                String name = book.name();
                LOGGER.info("THERE ARE ENCHENMENTS: {} {}", name, lvl);

                Integer requiredLevel = tradEntry.get(name);
                if (requiredLevel != null && lvl >= requiredLevel) {
                    return new OfferData(name, lvl, singleTradeEntry.costA().getCount(), i);
                }
            }
        }

        return null;
    }
*///?} else {
private OfferData findMatch(List<MerchantTradeData.SingleTradeEntry> offers, Map<String, Integer> tradEntry) {
    for (int i = 0; i < offers.size(); ++i) {
        MerchantTradeData.SingleTradeEntry singleTradeEntry = offers.get(i);
        ItemStack result = singleTradeEntry.result();


        List<Enchantment> enchantedBooks = EnchantedBookUtils.readStoredEnchantments(result);

        for (var book : enchantedBooks) {
            int lvl = book.level();
            String enchId = book.name();
            LOGGER.info("THERE ARE ENCHENMENTS: {} {}", enchId, lvl);

            Integer requiredLevel = tradEntry.get(enchId);
            if (requiredLevel != null && lvl >= requiredLevel) {
                // costA count, trade index i
                return new OfferData(enchId, lvl, singleTradeEntry.costA().getCount(), i);
            }
        }
    }
    return null;
}
    //?}
}

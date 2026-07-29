package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.dataModel.EnchantBook;
import me.afk.librarianRoller.dataModel.MerchantTradeData;
import me.afk.librarianRoller.utils.MessageUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
            if (ModConfigManager.INSTANCE.getConfig().autoBuy) {
                ctx.setRollerPhase(RollerPhaseBuy.INSTANCE);
            } else {
                ctx.stop();
            }
        } else {
            ctx.setRollerPhase(RollerPhaseBreak.INSTANCE);
        }
    }

    private EnchantBook findMatch(List<MerchantTradeData.SingleTradeEntry> offers, Map<String, Integer> tradEntry) {
        //fixme some version like 1.20.1 use another way but datacomponents

        for (int i = 0; i < offers.size(); ++i) {
            MerchantTradeData.SingleTradeEntry singleTradeEntry = offers.get(i);
            ItemStack result = singleTradeEntry.result();

            if (!result.is(Items.ENCHANTED_BOOK)) continue;
            ItemEnchantments enchantments = result.get(DataComponents.STORED_ENCHANTMENTS);
            if (enchantments == null || enchantments.isEmpty()) continue;

            for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
                String name = entry.getKey().value().description().getString().trim().toLowerCase();
                int lvl = entry.getValue();
                LOGGER.info("THERE ARE ENCHENMENTS: {}{}", name, lvl);

                Integer requiredLevel = tradEntry.get(name);
                if (requiredLevel != null && lvl >= requiredLevel) {
                    return new EnchantBook(name, lvl, singleTradeEntry.costA().getCount(), i);
                }
            }
        }

        return null;
    }
}

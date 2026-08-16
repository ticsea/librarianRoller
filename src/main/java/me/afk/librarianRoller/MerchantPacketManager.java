package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.MerchantOfferSnapshot;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;

public class MerchantPacketManager {
    // Flag: has new unprocessed merchant trade data
    private MerchantOfferSnapshot pendingTradeData = null;
    private MerchantOfferSnapshot latestTradeSnapshot = null;

    // Called from Mixin (Network Thread)
    public void acceptMerchantPacket(ClientboundMerchantOffersPacket rawPacket) {
        // Parse & copy data here
        MerchantOfferSnapshot data = parseRawPacket(rawPacket);
        synchronized (this) {
            this.pendingTradeData = data;
        }
    }

    // Call FROM your state machine (Main Thread)
    // Consume pending data (clear pending flag after read)
    public MerchantOfferSnapshot tryConsumePendingTradeData() {
        synchronized (this) {
            MerchantOfferSnapshot temp = pendingTradeData;
            pendingTradeData = null;
            if(temp != null) {
                latestTradeSnapshot = temp;
            }
            return temp;
        }
    }

    public MerchantOfferSnapshot getLatestTradeSnapshot() {
        synchronized (this) {
            return latestTradeSnapshot;
        }
    }

    public void reset() {
        synchronized (this) {
            pendingTradeData = null;
            latestTradeSnapshot = null;
        }
    }

    private MerchantOfferSnapshot parseRawPacket(ClientboundMerchantOffersPacket packet) {
        MerchantOffers offers = packet.getOffers();
        List<MerchantOfferSnapshot.SingleTradeEntry> entries = new ArrayList<>();
        for (int i = 0; i < offers.size(); ++i) {
            MerchantOffer merchantOffer = offers.get(i);
            MerchantOfferSnapshot.SingleTradeEntry singleTradeEntry = MerchantOfferSnapshot.SingleTradeEntry.fromOffer(i, merchantOffer);
            entries.add(singleTradeEntry);
        }
        return new MerchantOfferSnapshot(entries);
    }
}
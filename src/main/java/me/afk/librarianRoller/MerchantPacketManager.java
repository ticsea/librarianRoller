package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.MerchantTradeData;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import java.util.List;

public class MerchantPacketManager {
    public static final MerchantPacketManager INSTANCE = new MerchantPacketManager();

    // Flag: has new unprocessed merchant trade data
    private volatile MerchantTradeData pendingTradeData = null;
    private MerchantTradeData latestTradeSnapshot = null;

    private MerchantPacketManager(){}

    // Called from Mixin (Network Thread)
    public synchronized void acceptMerchantPacket(ClientboundMerchantOffersPacket rawPacket) {
        // Parse & copy data here
        MerchantTradeData data = parseRawPacket(rawPacket);
        this.pendingTradeData = data;
    }

    // Call FROM your state machine (Main Thread)
    // Consume pending data (clear pending flag after read)
    public MerchantTradeData tryConsumePendingTradeData() {
        synchronized (this) {
            MerchantTradeData temp = pendingTradeData;
            pendingTradeData = null;
            if(temp != null) {
                latestTradeSnapshot = temp;
            }
            return temp;
        }
    }

    public MerchantTradeData getLatestTradeSnapshot() {
        return latestTradeSnapshot;
    }

    public void reset() {
        synchronized (this) {
            pendingTradeData = null;
            latestTradeSnapshot = null;
        }
    }

    private MerchantTradeData parseRawPacket(ClientboundMerchantOffersPacket packet) {
        List<MerchantTradeData.SingleTradeEntry> entries = packet.getOffers().stream()
                .map(MerchantTradeData.SingleTradeEntry::fromTradeOffer)
                .toList();
        return new MerchantTradeData(entries);
    }
}
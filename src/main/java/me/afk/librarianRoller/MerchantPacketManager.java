package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.MerchantOfferSnapshot;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MerchantPacketManager {
    // Lock-free cross-thread trade data bridge.
    // Writer: Mixin (network thread) via acceptMerchantPacket()
    // Reader: Main thread via tryConsumePendingTradeData()/getLatestTradeSnapshot()
    private final AtomicReference<MerchantOfferSnapshot> pendingTradeData = new AtomicReference<>();
    private final AtomicReference<MerchantOfferSnapshot> latestTradeSnapshot = new AtomicReference<>();

    // Called from Mixin (Network Thread)
    public void acceptMerchantPacket(ClientboundMerchantOffersPacket rawPacket) {
        // Parse & copy data here
        MerchantOfferSnapshot data = parseRawPacket(rawPacket);
        // Atomic publish: swing-reference ensures the reader always sees a consistent snapshot.
        pendingTradeData.set(data);
    }

    // Call FROM your state machine (Main Thread)
    // Consume pending data (clear pending flag after read)
    public MerchantOfferSnapshot tryConsumePendingTradeData() {
        // Atomic get-and-set: reader acquires the pending snapshot and atomically clears the flag.
        MerchantOfferSnapshot temp = pendingTradeData.getAndSet(null);
        if (temp != null) {
            latestTradeSnapshot.set(temp);
        }
        return temp;
    }

    public MerchantOfferSnapshot getLatestTradeSnapshot() {
        // Atomic volatile read.
        return latestTradeSnapshot.get();
    }

    public void reset() {
        pendingTradeData.set(null);
        latestTradeSnapshot.set(null);
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
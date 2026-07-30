package me.afk.librarianRoller;

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenScreenPacketManager {
    public static final Logger LOGGER = LoggerFactory.getLogger("OpenScreenManager");

    // Store unprocessed open screen packet data
    private ClientboundOpenScreenPacket pendingPacket;

    // Call from Mixin (NETWORK THREAD)
    public synchronized void acceptPacket(ClientboundOpenScreenPacket packet) {
        this.pendingPacket = packet;
        LOGGER.debug("[OpenScreenManager] Captured ClientboundOpenScreenPacket, menuType: {}", packet.getType());
    }

    // Call in your state tick (MAIN THREAD)
    // Consume and clear pending packet, return null if nothing new
    public synchronized ClientboundOpenScreenPacket tryConsumePendingPacket() {
        ClientboundOpenScreenPacket temp = pendingPacket;
        pendingPacket = null;
        return temp;
    }

    // Clear leftover data before new roll cycle (critical to avoid stale packet)
    public synchronized void reset() {
        pendingPacket = null;
    }
}
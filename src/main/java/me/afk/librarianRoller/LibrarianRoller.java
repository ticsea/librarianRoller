package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ConfigService;
import me.afk.librarianRoller.config.ModConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibrarianRoller {
    public static final String MOD_ID = "librarianroller";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final ModConfigManager MOD_CONFIG_MANAGER = new ModConfigManager();
    private static final ConfigService CONFIG_SERVICE = new ConfigService();
    private static final MerchantPacketManager MERCHANT_PACKET_MANAGER = new MerchantPacketManager();
    private static final RollerContext ROLLER_CONTEXT = new RollerContext(MERCHANT_PACKET_MANAGER, MOD_CONFIG_MANAGER);

    private LibrarianRoller() {
    }

    public static ModConfigManager getModConfigManager() {
        return MOD_CONFIG_MANAGER;
    }

    public static ConfigService getConfigService() {
        return CONFIG_SERVICE;
    }

    public static MerchantPacketManager getMerchantPacketManager() {
        return MERCHANT_PACKET_MANAGER;
    }

    public static RollerContext getRollerContext() {
        return ROLLER_CONTEXT;
    }

    //fixme roll过程中背包里的讲台有可能消失(可能是网络同步问题)
    //fixme 有时候会停留在同一个村民 持续挖掘那个村民的讲台
}
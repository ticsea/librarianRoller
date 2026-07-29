package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibrarianRoller {
    public static final String MOD_ID = "librarianroller";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ModConfigManager MODCONFIGMANAGER = new ModConfigManager();
    public static final MerchantPacketManager MERCHANTPACKETMANAGER = new MerchantPacketManager();
    public static final RollerContext ROLLERCONTEXT = new RollerContext(MERCHANTPACKETMANAGER, MODCONFIGMANAGER);

    //fixme roll过程中背包里的讲台有可能消失(可能是网络同步问题)
    //fixme 有时候会停留在同一个村民 持续挖掘那个村民的讲台
}
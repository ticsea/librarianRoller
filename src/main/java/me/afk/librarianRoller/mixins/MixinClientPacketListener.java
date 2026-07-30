package me.afk.librarianRoller.mixins;

import me.afk.librarianRoller.*;
import me.afk.librarianRoller.config.ModConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {
//    @Unique
//    private final static Logger LOGGER = LoggerFactory.getLogger("PacketListener");


    @Inject(method = "handleOpenScreen", at = @At("HEAD"), cancellable = true)
    private void handleOpenScreen(ClientboundOpenScreenPacket clientboundOpenScreenPacket, CallbackInfo ci) {
        //fixme there are big error witha autobuy
        var rollerContext = LibrarianRoller.ROLLERCONTEXT;

        if (rollerContext.getEnabled() && rollerContext.getRollerPhase() != RollerPhaseBuy.INSTANCE) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection == null) return;

            connection.send(new ServerboundContainerClosePacket(clientboundOpenScreenPacket.getContainerId()));

            ci.cancel();

        } else if (LibrarianRoller.MODCONFIGMANAGER.getConfig().autoBuy && rollerContext.getRollerPhase() == RollerPhaseBuy.INSTANCE && clientboundOpenScreenPacket.getType() == MenuType.MERCHANT) {
            rollerContext.setMerchantScreenId(clientboundOpenScreenPacket.getContainerId());
        }
    }

    @Inject(method = "handleMerchantOffers", at = @At("TAIL"))
    private void handleMerchantOffers(ClientboundMerchantOffersPacket arg, CallbackInfo ci) {
        var rollerContext = LibrarianRoller.ROLLERCONTEXT;
        rollerContext.getMerchantPacketManager().acceptMerchantPacket(arg);
    }
}

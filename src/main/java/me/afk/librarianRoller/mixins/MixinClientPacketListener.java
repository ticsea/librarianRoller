package me.afk.librarianRoller.mixins;

import me.afk.librarianRoller.LibrarianRoller;
import me.afk.librarianRoller.RollerContext;
import me.afk.librarianRoller.ScreenIntent;
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

    // P1 (Mixin 收窄): the Mixin no longer inspects the roller phase. It is driven purely by
    // intent flags set by the state machine on the main thread:
    //   - awaitingMerchantScreen: an automated interaction is in flight -> cancel the OpenScreen.
    //   - buyScreenPending:       an autoBuy re-interaction is in flight -> let the MerchantScreen
    //                             through and record its container id.
    // This removes the phase-identity race between the network and main threads.
    @Inject(method = "handleOpenScreen", at = @At("HEAD"), cancellable = true)
    private void handleOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        RollerContext rollerContext = LibrarianRoller.getRollerContext();
        if (!rollerContext.isEnabled()) return;

        ScreenIntent intent = rollerContext.getScreenIntent();

        // autoBuy re-interaction: let the MerchantScreen through and record its container id.
        if (intent.consumeBuyScreenPending()) {
            if (packet.getType() == MenuType.MERCHANT) {
                rollerContext.setMerchantScreenId(packet.getContainerId());
            }
            return;
        }

        // Automated interaction: cancel the OpenScreen so it does not pop up.
        if (intent.consumeAwaitingMerchantScreen()) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection == null) return;
            connection.send(new ServerboundContainerClosePacket(packet.getContainerId()));
            ci.cancel();
        }
    }

    @Inject(method = "handleMerchantOffers", at = @At("TAIL"))
    private void handleMerchantOffers(ClientboundMerchantOffersPacket arg, CallbackInfo ci) {
        var rollerContext = LibrarianRoller.getRollerContext();
        rollerContext.getMerchantPacketManager().acceptMerchantPacket(arg);
    }
}
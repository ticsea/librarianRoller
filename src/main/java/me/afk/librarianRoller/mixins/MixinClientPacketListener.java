package me.afk.librarianRoller.mixins;

import me.afk.librarianRoller.Roller;
import me.afk.librarianRoller.config.ModConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static me.afk.librarianRoller.config.ModConfigManager.getEntry;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Unique
    private int enchantmentIndex$librarianroller;

    @Inject(method = "handleOpenScreen", at = @At("HEAD"), cancellable = true)
    private void handleOpenScreen(ClientboundOpenScreenPacket clientboundOpenScreenPacket, CallbackInfo ci) {
        if (Roller.getIsEnabled()) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection == null) return;

            connection.send(new ServerboundContainerClosePacket(clientboundOpenScreenPacket.getContainerId()));

            ci.cancel();
        } else if (ModConfigManager.getConfig().autoBuy && clientboundOpenScreenPacket.getType() == MenuType.MERCHANT) {
            Minecraft instance = Minecraft.getInstance();
            LocalPlayer player = instance.player;
            var connection = instance.getConnection();
            MultiPlayerGameMode gameMode = instance.gameMode;
            if (connection == null || gameMode == null) return;

            connection.send(new ServerboundSelectTradePacket(enchantmentIndex$librarianroller));
            instance.execute(() -> gameMode.handleInventoryMouseClick(clientboundOpenScreenPacket.getContainerId(), 2, 0, ClickType.PICKUP, player));
        }
    }

    @Inject(method = "handleMerchantOffers", at = @At("TAIL"))
    private void handleMerchantOffers(ClientboundMerchantOffersPacket arg, CallbackInfo ci) {
        if (!Roller.getIsEnabled()) return;
        if (arg.getVillagerXp() >  1) return;//todo fixme this

        MerchantOffers offers = arg.getOffers();

        offers.forEach(it -> {
            ItemStack result = it.getResult();
            if (result.is(Items.ENCHANTED_BOOK)) {
                ItemEnchantments itemEnchantments = result.get(DataComponents.STORED_ENCHANTMENTS);
                assert itemEnchantments != null;

                for (Map.Entry<Holder<Enchantment>, Integer> entry : itemEnchantments.entrySet()) {
                    var name = entry.getKey().value().description().getString().trim().toLowerCase();
                    int lvl = entry.getValue();
//                    LOGGER.info("THERE ARE ENCHENMENTS: " + name + lvl);

                    if (getEntry().containsKey(name)) {
                        Integer i = getEntry().get(name);
                        if (lvl >= i) {
                            enchantmentIndex$librarianroller = offers.getFirst().getResult().is(Items.ENCHANTED_BOOK)? 0 : 1;
                            Roller.buy();

                            Roller.stop();
                            Roller.printReward(name, lvl, it.getCostA().getCount());
                        }
                    }
                }
            }
        });
    }
}

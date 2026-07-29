package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.ClickType;

import java.util.List;

public class RollerPhaseBuy implements IRollerPhase{
    public static final RollerPhaseBuy INSTANCE = new RollerPhaseBuy();

    @Override
    public void doAction(RollerContext ctx) {
        if (!ctx.getEnabled()) return;

        Minecraft minecraft = ctx.getMinecraft();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null)return;
        if (minecraft.screen instanceof MerchantScreen) {

            minecraft.getConnection().send(new ServerboundSelectTradePacket(ctx.getEnchantBook().index()));
            gameMode.handleInventoryMouseClick(ctx.getMerchantScreenId(), 2, 0, ClickType.PICKUP, player);
            ctx.stop();
        }


        List<VillagerAndLectern> list = ctx.getList();
        if (list.isEmpty()) return;
        int pairIndex = ctx.getPairIndex();
        Villager villager = list.get(pairIndex).villager();
        if (villager == null) return;
        gameMode.interact(player, villager, InteractionHand.MAIN_HAND);
    }
}

package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.InteractionHand;
//? if > 1.21.1 {
import net.minecraft.world.entity.npc.villager.Villager;
//?} else {

/*import net.minecraft.world.entity.npc.Villager;
        *///?}

import net.minecraft.world.inventory.ClickType;

import java.util.List;

public class RollerPhaseBuy implements IRollerPhase {
    private final RollerState state;
    private final RollerTransitions transitions;

    public RollerPhaseBuy(RollerState state, RollerTransitions transitions) {
        this.state = state;
        this.transitions = transitions;
    }

    @Override
    public void doAction() {
        if (!state.isEnabled()) return;

        Minecraft minecraft = state.getMinecraft();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) return;
        if (minecraft.screen instanceof MerchantScreen) {

            minecraft.getConnection().send(new ServerboundSelectTradePacket(state.getEnchantBook().index()));
            gameMode.handleInventoryMouseClick(state.getMerchantScreenId(), 2, 0, ClickType.PICKUP, player);
            transitions.stop();
            // NOTE: stop() resets the context (clears the list). Return explicitly so we
            // don't fall through to the re-interaction below, which would otherwise run
            // against a stale/cleared list.
            return;
        }


        List<VillagerAndLectern> list = state.getList();
        if (list.isEmpty()) return;
        int pairIndex = state.getPairIndex();
        Villager villager = list.get(pairIndex).villager();
        if (villager == null) return;
        // P1: signal the Mixin that this is an autoBuy re-interaction, so it lets the
        // MerchantScreen through and records its container id for the purchase click.
        transitions.getScreenIntent().setBuyScreenPending(true);
        gameMode.interact(player, villager, InteractionHand.MAIN_HAND);
    }
}
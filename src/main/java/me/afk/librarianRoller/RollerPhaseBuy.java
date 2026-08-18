package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.Librarians;
import me.afk.librarianRoller.utils.InteractionUtils;
import me.afk.librarianRoller.utils.PlayerInventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if > 1.21.1 {
import net.minecraft.world.entity.npc.villager.Villager;
//?} else {

/*import net.minecraft.world.entity.npc.Villager;
        *///?}

//~ if >= 26.1 'ClickType' -> 'ContainerInput' {
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.EntityHitResult;
        //~}

import java.util.List;

/**
 * BUY phase - a stateless pure executor.
 * <p>
 * Re-opens the villager's MerchantScreen (the automated interaction in INTERACT
 * canceled it), selects the matched trade and clicks the buy button. Emits
 * {@link RollerEvent.Type#BUY_COMPLETE} after the purchase; the context then
 * stops the roller. While waiting for the MerchantScreen to open it emits
 * {@link RollerEvent.Type#WAITING} (stay in BUY).
 */
public class RollerPhaseBuy implements IRollerPhase {
    private final RollerState state;
    private final RollerTransitions transitions;

    public RollerPhaseBuy(RollerState state, RollerTransitions transitions) {
        this.state = state;
        this.transitions = transitions;
    }

    @Override
    public RollerEvent doAction() {
        if (!state.isEnabled()) return RollerEvent.of(RollerEvent.Type.WAITING);

        Minecraft minecraft = state.getMinecraft();
        if (minecraft == null) return RollerEvent.of(RollerEvent.Type.WAITING);
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) return RollerEvent.of(RollerEvent.Type.WAITING);
        //~ if >= 26.2 'screen' -> 'gui.screen()'
        if (minecraft.screen instanceof MerchantScreen) {
            if (state.getEnchantBook() == null) return RollerEvent.of(RollerEvent.Type.WAITING);
            minecraft.getConnection().send(new ServerboundSelectTradePacket(state.getEnchantBook().index()));
            PlayerInventoryUtils.containerInput(state.getMerchantScreenId(), 2, 0, player);

            // The context stops the roller on BUY_COMPLETE (stop() resets the context,
            // clearing the list - the context is the only one allowed to stop now).
            return RollerEvent.of(RollerEvent.Type.BUY_COMPLETE);
        }

        List<Librarians> list = state.getList();
        if (list.isEmpty()) return RollerEvent.of(RollerEvent.Type.WAITING);
        int pairIndex = state.getPairIndex();
        Villager villager = list.get(pairIndex).villager();
        if (villager == null) return RollerEvent.of(RollerEvent.Type.WAITING);
        // Signal the Mixin that this is an autoBuy re-interaction, so it lets the
        // MerchantScreen through and records its container id for the purchase click.
        long epoch = transitions.getScreenIntent().set(ScreenIntent.Mode.ALLOW_AND_RECORD);
        InteractionResult result = InteractionUtils.interactVillager(player, villager);

        // If the interact fails synchronously, no OpenScreen will arrive - clear the
        // intent so a later spurious OpenScreen cannot be mis-consumed.
        if (result != InteractionResult.SUCCESS) {
            transitions.getScreenIntent().consume(epoch);
        }
        // Stay in BUY and wait for the MerchantScreen to open next tick.
        return RollerEvent.of(RollerEvent.Type.WAITING);
    }
}
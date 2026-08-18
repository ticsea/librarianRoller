package me.afk.librarianRoller;

import me.afk.librarianRoller.dataModel.Librarians;
import me.afk.librarianRoller.utils.InteractionUtils;
import me.afk.librarianRoller.utils.PlayerInventoryUtils;
import me.afk.librarianRoller.utils.VillagerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if > 1.21.1 {
import net.minecraft.world.entity.npc.villager.Villager;
//?} else {

/*import net.minecraft.world.entity.npc.Villager;
        *///?}
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

/**
 * INTERACT phase - a stateless pure executor.
 * <p>
 * Attempts an automated interaction with the current villager. Sets the
 * {@link ScreenIntent} CANCEL intent before {@code gameMode.interact()} so the Mixin
 * suppresses the resulting OpenScreen (headless rolling). Emits an event; the context
 * owns all transitions and mutable state.
 */
public class RollerPhaseInteract implements IRollerPhase {
    private final RollerState state;
    private final RollerTransitions transitions;

    public RollerPhaseInteract(RollerState state, RollerTransitions transitions) {
        this.state = state;
        this.transitions = transitions;
    }

    @Override
    public RollerEvent doAction() {
        if (!state.isEnabled()) return RollerEvent.of(RollerEvent.Type.WAITING);

        Minecraft instance = state.getMinecraft();
        if (instance == null) return RollerEvent.of(RollerEvent.Type.WAITING);
        LocalPlayer player = instance.player;
        MultiPlayerGameMode gameMode = instance.gameMode;
        Level level = instance.level;
        if (player == null || gameMode == null || level == null) return RollerEvent.of(RollerEvent.Type.WAITING);
        List<Librarians> list = state.getList();
        if (list.isEmpty()) return RollerEvent.of(RollerEvent.Type.WAITING);
        int pairIndex = state.getPairIndex();
        if (pairIndex >= list.size()) {
            // Wrap around defensively - WAITING keeps us in INTERACT.
            return RollerEvent.of(RollerEvent.Type.WAITING);
        }

        // Two cases if the interaction result is not SUCCESS:
        // 1. The villager lost its profession but the lectern is still present.
        // 2. The lectern is gone (block == AIR).
        Librarians villagerAndLectern = list.get(pairIndex);
        if (villagerAndLectern == null) return RollerEvent.of(RollerEvent.Type.WAITING);
        BlockPos blockPos = villagerAndLectern.lecternPos();
        if (blockPos == null) return RollerEvent.of(RollerEvent.Type.WAITING);
        Block block = level.getBlockState(blockPos).getBlock();
        if (block == Blocks.AIR) {
            // Lectern is missing - go to PLACE to replace it.
            return RollerEvent.of(RollerEvent.Type.LECTERN_MISSING);
        }

        Villager villager = villagerAndLectern.villager();
        if (villager == null || !VillagerUtils.isLibrarian(villager)) return RollerEvent.of(RollerEvent.Type.WAITING);

        if (!PlayerInventoryUtils.swapAxe(player)) {
            // No axe available right now - stop.
            return RollerEvent.of(RollerEvent.Type.FATAL);
        }

        // Invalidate any stale trade snapshot BEFORE interacting: only the offers from
        // THIS interaction may be parsed by PARSE. A leftover snapshot (from manual
        // trading while stopped, or from the previous villager round) would otherwise
        // be matched against the wrong villager (or stall matching entirely).
        transitions.getMerchantPacketManager().reset();

        // Signal the Mixin that an automated interaction is in flight, so it cancels
        // the resulting OpenScreen (the roller runs headless).
        long epoch = transitions.getScreenIntent().set(ScreenIntent.Mode.CANCEL);
        InteractionResult result = InteractionUtils.interactVillager(player, villager);
        player.swing(InteractionHand.MAIN_HAND);

        if (result == InteractionResult.SUCCESS) {
            // The OpenScreen packet is expected to be consumed by the Mixin.
            return RollerEvent.of(RollerEvent.Type.INTERACT_SUCCESS);
        } else {
            // Interaction failed - no OpenScreen will arrive. Clear the intent now so a
            // later spurious OpenScreen cannot be mis-consumed (epoch check in the Mixin).
            transitions.getScreenIntent().consume(epoch);
            // Retry next tick (the transition table maps INTERACT_FAILED back to INTERACT).
            return RollerEvent.of(RollerEvent.Type.INTERACT_FAILED);
        }
    }
}
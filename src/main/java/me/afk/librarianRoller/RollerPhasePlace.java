package me.afk.librarianRoller;

import me.afk.librarianRoller.utils.MessageUtils;
import me.afk.librarianRoller.utils.PlayerInventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class RollerPhasePlace implements IRollerPhase{
    public static final RollerPhasePlace INSTANCE = new RollerPhasePlace();

    private RollerPhasePlace() {
    }

    @Override
    public void doAction(RollerContext ctx) {
        if (!ctx.getEnabled()) return;
        //        Minecraft instance = Minecraft.getInstance();
        Minecraft instance = ctx.getMinecraft();
        LocalPlayer player = instance.player;
        MultiPlayerGameMode interactionManager = instance.gameMode;
        Level level = instance.level;
        int pairIndex = ctx.getPairIndex();
        List<VillagerAndLectern> list = ctx.getList();

        if (player == null || level == null || list.isEmpty() || interactionManager == null) return;


        if (!player.getOffhandItem().is(Items.LECTERN)) {
            //fixme 发生在捡起讲台的瞬间,但还没进入背包.之后讲台进入背包.
            //情况1: 讲台已经被交换到左手,依旧报错找不到
            if (!PlayerInventoryUtils.swapItem(player, EquipmentSlot.OFFHAND, item -> item == Items.LECTERN)) {

                MessageUtils.throwError("afk.enchant_roller.error.not_found_lectern");
                this.stop(ctx);
                return;
            }
        }

        BlockHitResult hitResult = list.get(pairIndex).lecternBelowHitResult();
        if (hitResult.getType() == HitResult.Type.MISS) {
            this.stop(ctx);
            return;
        }

        var result = interactionManager.useItemOn(player, InteractionHand.OFF_HAND, hitResult);

        if (result == InteractionResult.SUCCESS) {
            pairIndex = (pairIndex + 1) % list.size();
            ctx.setPairIndex(pairIndex);
        }

        ctx.setRollerPhase(RollerPhaseInteract.INSTANCE);
    }
}

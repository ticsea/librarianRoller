package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.dataModel.VillagerAndLectern;
import me.afk.librarianRoller.utils.MessageUtils;
import me.afk.librarianRoller.utils.villagerAndLectern.IRollerMode;
import me.afk.librarianRoller.utils.villagerAndLectern.RollerModeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IRollerPhase {
    default void toggle(RollerContext ctx) {
        boolean enabled = ctx.getEnabled();
        ctx.setEnabled(!enabled);
        enabled = ctx.getEnabled();

        if (!enabled) {
            stop(ctx);
            return;
        }

        start(ctx);
    }

    default void start(RollerContext ctx) {
        Minecraft minecraft = ctx.getMinecraft();
        if (minecraft == null) return;
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode interactionManager = minecraft.gameMode;

        if (player == null || interactionManager == null) {
            stop(ctx);
            return;
        }

        ModConfig modConfig = ctx.getModConfig();
        List<VillagerAndLectern> foundVillagers = getFoundVillagers(modConfig);


        if (foundVillagers.isEmpty()) {
            stop(ctx);
            MessageUtils.throwError("afk.enchant_roller.error.not_found_villager_or_lectern", Component.literal(modConfig.rollerMode));
            return;
        }

        if (modConfig.autoBuy) {
            if (!hasSufficientResources(player)) {
                stop(ctx);
                MessageUtils.throwError("afk.enchant_roller.error.not_enough_emerald_or_book");
                return;
            }
        }

        ctx.getModConfigManager().setEntry();
        ctx.setList(foundVillagers);
        //        OpenScreenPacketManager.INSTANCE.reset();
        //        ctx.reset();

        MessageUtils.print("afk.enchant_roller.info.turnon");
    }

    private static @NotNull List<VillagerAndLectern> getFoundVillagers(ModConfig modConfig) {
        return RollerModeRegistry.getRollerModes().stream().filter(m -> m.getName().equals(modConfig.rollerMode)).findFirst().map(IRollerMode::find).orElse(List.of());
    }

    private boolean hasSufficientResources(LocalPlayer player) {
        int emeraldCount = player.getInventory().countItem(Items.EMERALD);
        int bookCount = player.getInventory().countItem(Items.BOOK);
        return emeraldCount >= 64 && bookCount >= 1;
    }

    default void stop(RollerContext ctx) {
        ctx.reset();
        MessageUtils.print("afk.enchant_roller.info.turnoff");
    }

    void doAction(RollerContext ctx);
}

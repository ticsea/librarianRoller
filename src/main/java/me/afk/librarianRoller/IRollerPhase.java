package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.config.ModeVType;
import me.afk.librarianRoller.utils.MessageUtils;
import me.afk.librarianRoller.utils.villagerAndLectern.IRollerMode;
import me.afk.librarianRoller.utils.villagerAndLectern.RollerModeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
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

        ModConfigManager modConfigManager = ctx.getModConfigManager();
        ModConfig modConfig = ctx.getModConfig();

        List<VillagerAndLectern> foundVillagers = RollerModeRegistry.getRollerModes().stream()
                .filter(m -> m.getName().equals(modConfig.rollerMode))
                .findFirst()
                .map(IRollerMode::find)
                .orElse(List.of());


        if (foundVillagers.isEmpty()) {
            MessageUtils.throwError("afk.enchant_roller.error.not_found_villager_or_lectern", Component.literal(modConfig.rollerMode));

            stop(ctx);
            return;
        }

        ctx.setPairIndex(0);
        ctx.setList(foundVillagers);
        modConfigManager.setEntry();

        if (modConfig.autoBuy) {
            if (!hasSufficientResources(player)) {
                MessageUtils.throwError("afk.enchant_roller.error.not_enough_emerald_or_book");

                stop(ctx);
                return;
            }

            //todo test
            if (modConfig.modeVType == ModeVType.SINGLE) {
                ctx.setTimeToBuy(1);
            } else if (modConfig.modeVType == ModeVType.CONTINUE) {
                int time = RollerModeRegistry.getRollerModes().stream()
                        .filter(m -> m.getName().equals(modConfig.rollerMode))
                        .findFirst()
                        .map(IRollerMode::getRequireCount)
                        .orElse(1);
                ctx.setTimeToBuy(time);
            }
        }

        MessageUtils.print("afk.enchant_roller.info.turnon");
    }

    private boolean hasSufficientResources(LocalPlayer player) {
        int emeraldCount = player.getInventory().countItem(Items.EMERALD);
        int bookCount = player.getInventory().countItem(Items.BOOK);
        return emeraldCount >= 64 && bookCount >= 1;
    }

    default void stop(RollerContext ctx) {
        ctx.setEnabled(false);

        MessageUtils.print("afk.enchant_roller.info.turnoff");


    }

    void doAction(RollerContext ctx);
}

package me.afk.librarianRoller.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;


import java.util.function.Predicate;

public class PlayerInventoryUtils {

    public static boolean swapItem(LocalPlayer player, EquipmentSlot handSlot, Predicate<Item> predicate) {
        boolean state = false;
        if (!predicate.test(player.getItemBySlot(handSlot).getItem())) {
            MultiPlayerGameMode interactionManager = Minecraft.getInstance().gameMode;
            if (interactionManager == null) return state;

            if (player.inventoryMenu == player.containerMenu) {
                state = swapFromInventory(player, interactionManager, predicate, handSlot);
            } else {
                state = swapFromHotbar(player, handSlot, predicate);
            }
        }

        return state;
    }

    private static boolean swapFromInventory(LocalPlayer player, MultiPlayerGameMode interactionManager, Predicate<Item> predicate, EquipmentSlot handSlot) {
        boolean state = false;

        for (int i = 9; i <= 44; i++) {
            int slot = i >= 36 ? i - 36 : i;
            var stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && predicate.test(stack.getItem())) {
                boolean hasHandItem = !player.getItemBySlot(handSlot).isEmpty();
                //here is mess.
                int selectSlot = handSlot == EquipmentSlot.OFFHAND ? 45 : getSelect(player) + 36;

                interactionManager.handleInventoryMouseClick(
                        player.containerMenu.containerId,
                        i,
                        0,
                        ClickType.PICKUP,
                        player
                );
                interactionManager.handleInventoryMouseClick(
                        player.containerMenu.containerId,
                        selectSlot,
                        0,
                        ClickType.PICKUP,
                        player
                );

                if (hasHandItem) {
                    interactionManager.handleInventoryMouseClick(
                            player.containerMenu.containerId,
                            i,
                            0,
                            ClickType.PICKUP,
                            player
                    );
                }

                state = true;

                break;
            }
        }

        return state;
    }

    private static boolean swapFromHotbar(LocalPlayer player, EquipmentSlot handSlot, Predicate<Item> predicate) {
        boolean state = false;

        for (int i = 0; i <= 8; i++) {
            var stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && predicate.test(stack.getItem())) {
                setSelect(player, i);

                if (handSlot == EquipmentSlot.OFFHAND) {
                    player.connection.send(
                            new ServerboundPlayerActionPacket(
                                    ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                                    BlockPos.ZERO,
                                    Direction.DOWN
                            )
                    );
                }

                state = true;

                break;
            }
        }

        return state;
    }

    public static int getSelect(LocalPlayer player) {
        //? if >=1.21.11 {
        return player.getInventory().getSelectedSlot();
        //?} else {
        /*return player.getInventory().selected;
        *///?}
    }

    public static void setSelect(LocalPlayer player, int slot) {
        if (getSelect(player) == slot) return;
        //? if >=1.21.11 {
        player.getInventory().setSelectedSlot(slot);
        player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        //?} else {
        /*player.getInventory().selected = slot;
        player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        *///?}
    }
}

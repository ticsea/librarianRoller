package me.afk.librarianRoller.utils;
//~ if >= 26.1 'ClickType' -> 'ContainerInput' {
import me.afk.librarianRoller.LibrarianRoller;
import me.afk.librarianRoller.RollerTransitions;
import me.afk.librarianRoller.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.function.Predicate;

public class PlayerInventoryUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerInventoryUtils-" + LibrarianRoller.MOD_ID);

    private static final int PLAYER_INV_STORAGE_START_SLOT = 9;
    private static final int PLAYER_INV_STORAGE_END_SLOT = 45;
    private static final int PLAYER_INV_HOTBAR_CONTAINER_START = 36;
    private static final int PLAYER_INV_OFFHAND_CONTAINER_SLOT = 45;
    private static final int HOTBAR_SIZE = 9;

    public static boolean swapAxe(LocalPlayer player) {
        if (!PlayerInventoryUtils.swapItem(player, EquipmentSlot.MAINHAND, itemHere -> itemHere instanceof AxeItem)) {
            MessageUtils.throwError("afk.enchant_roller.error.not_found_axe");
            return false;
        }

        return true;
    }

    public static boolean preventAxeBreaking(LocalPlayer player, ModConfig modConfig) {
//        boolean shouldStop = true;

        var stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof AxeItem)) {
            MessageUtils.throwError("afk.enchant_roller.error.is_not_axe");
//            transitions.stop();

//            return shouldStop;
            return true;
        };

        if (modConfig.preventAxeBreaking) {
            int i = stack.getMaxDamage() - stack.getDamageValue();
            if (i <= 10) {
                MessageUtils.throwError("afk.enchant_roller.warn.low_damage");
//                transitions.stop();
                return true;
            }
        }

        return false;
    }

    public static boolean swapItem(LocalPlayer player, EquipmentSlot handSlot, Predicate<Item> predicate) {
        boolean success = false;

        if (!predicate.test(player.getItemBySlot(handSlot).getItem())) {
            Minecraft instance = Minecraft.getInstance();
            if (instance == null) {
                LOGGER.error("Minecraft instance is null, abort swap");
                return false;
            }

            MultiPlayerGameMode interactionManager = instance.gameMode;
            if (interactionManager == null) {
                //? if > 1.21.1 {
                LOGGER.error("GameMode null, player={}, slot={}", player.getGameProfile().name(), handSlot);
                //?} else {
                /*LOGGER.error("GameMode null, player={}, slot={}", player.getGameProfile().getName(), handSlot);
                *///?}

                return false;
            };

            ItemStack currentHandStack = player.getItemBySlot(handSlot);
            if (predicate.test(currentHandStack.getItem())) {
                return true;
            }


            if (player.inventoryMenu == player.containerMenu) {
                success = swapFromInventory(player, interactionManager, predicate, handSlot);
            } else {
                success = swapFromHotbar(player, handSlot, predicate);
            }
        } else {
            success = true;
        }

        return success;
    }

    private static boolean swapFromInventory(LocalPlayer player, MultiPlayerGameMode interactionManager, Predicate<Item> predicate, EquipmentSlot handSlot) {
        boolean success = false;

        Inventory inventory = player.getInventory();
        int targetSlot = handSlot == EquipmentSlot.OFFHAND ? PLAYER_INV_OFFHAND_CONTAINER_SLOT : getSelect(player) + PLAYER_INV_HOTBAR_CONTAINER_START;

        for (int i = PLAYER_INV_STORAGE_START_SLOT; i < PLAYER_INV_STORAGE_END_SLOT; ++i) {
            int slot = i >= PLAYER_INV_HOTBAR_CONTAINER_START ? i - PLAYER_INV_HOTBAR_CONTAINER_START : i;
            var stack = inventory.getItem(slot);

            if (stack.isEmpty() || !predicate.test(stack.getItem())) continue;

            //here is mess.
            boolean hasHandItem = !player.getItemBySlot(handSlot).isEmpty();
            containerInput(
                    player.containerMenu.containerId,
                    i,
                    0,
                    ClickType.PICKUP,
                    player
            );
            containerInput(
                    player.containerMenu.containerId,
                    targetSlot,
                    0,
                    ClickType.PICKUP,
                    player
            );

            if (hasHandItem && inventory.getItem(slot).isEmpty()) {
                containerInput(
                        player.containerMenu.containerId,
                        i,
                        0,
                        ClickType.PICKUP,
                        player
                );
            }

            //todo check here is saft?
            player.containerMenu.broadcastChanges();

            success = true;
            break;
        }

        return success;
    }

    public static void containerInput(
            int containerId,
            int slot,
            int button,
            ClickType action,
            LocalPlayer player
    ) {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) return;
        //? if >= 26.1 {
        /*gameMode.handleClickType(containerId, slot, button, action, player);
        *///?} else {
        gameMode.handleInventoryMouseClick(containerId, slot, button, action, player);
        //?}
    }

    public static void containerInput(
            int containerId,
            int slot,
            int button,
            LocalPlayer player
    ) {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) return;
        //? if >= 26.1 {
        /*gameMode.handleClickType(containerId, slot, button, ClickType.PICKUP, player);
        *///?} else {
        gameMode.handleInventoryMouseClick(containerId, slot, button, ClickType.PICKUP, player);
         //?}
    }

    private static boolean swapFromHotbar(LocalPlayer player, EquipmentSlot handSlot, Predicate<Item> predicate) {
        boolean success = false;
        Inventory inventory = player.getInventory();

        for (int i = 0; i < 9; ++i) {
            var stack = inventory.getItem(i);

            if (stack.isEmpty() || !predicate.test(stack.getItem())) continue;

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

            success = true;
            // Only handle the first matching slot - otherwise we'd keep switching the
            // selected slot and (for OFFHAND) send multiple swap packets.
            break;
        }

        return success;
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
//~}

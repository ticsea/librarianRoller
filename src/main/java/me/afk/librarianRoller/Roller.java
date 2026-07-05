package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.config.RollerType;
import me.afk.librarianRoller.utils.MessageUtils;
import me.afk.librarianRoller.utils.PlayerInventoryUtils;
//? if FABRIC {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
        //?}
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
//? if >=1.21.11 {
import net.minecraft.world.entity.npc.villager.Villager;
        //?} else {
/*import net.minecraft.world.entity.npc.Villager;
        *///?}
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
//? if NEOFORGE {
/*import net.neoforged.neoforge.client.event.ClientTickEvent;
*///?}
import java.util.List;


import static me.afk.librarianRoller.config.ModConfigManager.getConfig;
import static me.afk.librarianRoller.config.ModConfigManager.setEntry;
import static me.afk.librarianRoller.utils.FindVillagerAndLectern.findLecternAndVillager;
import static me.afk.librarianRoller.utils.VillagerUtils.isCorrectVillagerProfession;


public class Roller {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static boolean isEnabled = false;
    private static int pairIndex = 0;
    private static State state = State.IDLE;
    private static List<VillagerAndLectern> list = null;
    private static LocalPlayer player;
    private static Level level;
    private static MultiPlayerGameMode interactionManager;

    public static void start() {
        isEnabled = !isEnabled;

        if (!isEnabled) {
            stop();
            return;
        }

        player = minecraft.player;
        interactionManager = minecraft.gameMode;
        if (player == null || interactionManager == null) {
            stop();
            return;
        }

        level = player.level();
        var config = getConfig();

        // todo does this method check villager and lectern?
        List<VillagerAndLectern> foundVillagers = findLecternAndVillager(player, config.mode);

        if (!isValidConfiguration(config.mode, foundVillagers)) {
            stop();
            return;
        }

        // Set the found villagers
        list = foundVillagers;
        setEntry();

        // Validate resources for auto-buy
        if (config.autoBuy && !hasSufficientResources(player)) {
            stop();
            MessageUtils.throwError("afk.enchant_roller.error.not_enough_emerald_or_book");
        }

        MessageUtils.print("afk.enchant_roller.info.turnon");
    }

    private static boolean isValidConfiguration(RollerType mode, List<VillagerAndLectern> foundVillagers) {
        if (foundVillagers.isEmpty()) {
            return false;
        }

        int requiredCount = getRequiredVillagerCount(mode);
        return foundVillagers.size() == requiredCount;
    }

    private static int getRequiredVillagerCount(RollerType mode) {
        return switch (mode) {
            case RollerType.V1 -> 1;
            case RollerType.V4 ->  4;
            case RollerType.V6 ->  6;
            default -> 0;
        };
    }

    //todo move to FindVillagerAndLecturen class.
    private static boolean hasSufficientResources(LocalPlayer player) {
        int emeraldCount = player.getInventory().countItem(Items.EMERALD);
        int bookCount = player.getInventory().countItem(Items.BOOK);
        return emeraldCount >= 64 && bookCount >= 1;
    }

    public static void stop() {
        isEnabled = false;
        MessageUtils.print("afk.enchant_roller.info.turnoff");


        // reset
        pairIndex = 0;
        list = null;
    }

    public static void buy() {
        if (ModConfigManager.getConfig().autoBuy) {
            interactionManager.interact(player, list.get(pairIndex).villager(), InteractionHand.MAIN_HAND);
        }
    }

    //? if NEOFORGE {
    /*public static void tick(ClientTickEvent.Post event) {
            tickEvent();
        }
    *///?} elif FABRIC {
    public static void fabricEvent() {
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            tickEvent();
        });
    }

    //?}

    private static void tickEvent() {
        if (!isEnabled) return;

        Villager villager = list.get(pairIndex).villager();

        switch (state) {
            case State.IDLE:
                idle(villager, player);
                break;

            case State.INTERACT:
                hitVillager(interactionManager, player, villager);
                break;

            case BREAK:
                breakA(player);
                break;

            case PLACE:
                place(player);
                break;
        }
    }

    private static void idle(Villager villager, LocalPlayer player) {
        //fixme it will throw nop exption when escape game.
        if (isCorrectVillagerProfession(villager)) {
            state = State.INTERACT;

        } else {
            // findVillagerAndLectern method should valid there are lectern and villager. so what solution will trigger this?
            // maybe the time villager has no profession? or player interact with villager too fast?
            // anyway , keep it.
            BlockPos lecternPos1 = list.get(pairIndex).lecternPos();
            var block = level.getBlockState(lecternPos1).getBlock();
            // check isn't air also?
            if (!(block instanceof LecternBlock)) {
                state = State.PLACE;
            }
        }
    }

    // should this method be extract?
    private static void hitVillager(MultiPlayerGameMode interactionManager, LocalPlayer player, Villager villager) {
        InteractionResult result = interactionManager.interact(player, villager, InteractionHand.MAIN_HAND);

        if (result == InteractionResult.SUCCESS) {
            state = State.BREAK;
        } else {
            state = State.IDLE;

            //which should i use?
            /*MessageUtils.throwError("afk.enchant_roller.error.interaction_failed");
            stop();*/
        }
    }

    private static void breakA(LocalPlayer player) {
        var stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof AxeItem)) {
            if (!(PlayerInventoryUtils.swapItem(player, EquipmentSlot.MAINHAND, itemhere -> itemhere instanceof AxeItem))){
                MessageUtils.throwError("afk.enchant_roller.error.not_found_axe");
                stop();
                return;
            }
        }

        stack = player.getMainHandItem();
        if (getConfig().preventAxeBreaking) {
            int i = stack.getMaxDamage() - stack.getDamageValue();
            if (i <= 10) {
                MessageUtils.throwError("afk.enchant_roller.warn.low_damage");
                stop();
                return;
            }
        }

        var currentPair = list.get(pairIndex);
        BlockPos lecternPos = currentPair.lecternPos();
        var blockState = level.getBlockState(lecternPos);

        // or blockState.getBlock() != Blocks.AIR?
        if (blockState.getBlock() instanceof LecternBlock) {

            if (interactionManager.continueDestroyBlock(lecternPos, Direction.UP)) {
            player.swing(InteractionHand.MAIN_HAND);
            }

        } else {
            state = State.PLACE;
        }
    }

    private static void place(LocalPlayer player) {
        if (!player.getOffhandItem().is(Items.LECTERN)) {
            if (PlayerInventoryUtils.swapItem(player, EquipmentSlot.OFFHAND, item -> item == Items.LECTERN)) return;

            MessageUtils.throwError("afk.enchant_roller.error.not_found_lectern");
            stop();
            return;
        }

        BlockHitResult hitResult = list.get(pairIndex).lecternBelowHitResult();
        if (hitResult.getType() == HitResult.Type.MISS) {
            stop();
            return;
        }

        var result = interactionManager.useItemOn(player, InteractionHand.OFF_HAND, hitResult);

        if (result == InteractionResult.SUCCESS) {
            pairIndex = (pairIndex + 1) % list.size();
        }

        state = State.IDLE;
    }

    public static boolean getIsEnabled() {
        return isEnabled;
    }

    public static void printReward(String translationKey, int level, int cost) {

        //fixme the color always is green
        int costColor;
        if (cost <= 16) {
            costColor = 5635925;
        } else if (cost <= 32) {
            costColor = 16777045;
        } else {
            costColor = 16733525;
        }
        MessageUtils.print("afk.enchant_roller.info.obtained_enchantment", Component.literal(translationKey).withColor(5635925), Component.literal(String.valueOf(level)).withStyle(ChatFormatting.GOLD), Component.literal(String.valueOf(cost)).withColor(costColor));
    }

    enum State{
        IDLE,
        INTERACT,
        BREAK,
        PLACE
    }
}


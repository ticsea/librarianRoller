package me.afk.librarianRoller;

import me.afk.librarianRoller.config.ModConfig;
import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.utils.MessageUtils;
import me.afk.librarianRoller.utils.PlayerInventoryUtils;
//? if FABRIC {
import me.afk.librarianRoller.utils.VillagerUtils;
import me.afk.librarianRoller.utils.villagerAndLectern.IRollerMode;
import me.afk.librarianRoller.utils.villagerAndLectern.RollerModeRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
        //?}
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
import java.util.Objects;

public class Roller {
    private static Roller INSTANCE;
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static ModConfigManager modConfigManager = ModConfigManager.getInstance();
    private static ModConfig modConfig;
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
        modConfig = modConfigManager.getConfig();

        List<VillagerAndLectern> foundVillagers = RollerModeRegistry.getRollerModes().stream()
                .filter(m -> m.getName().equals(modConfig.mode))
                .findFirst()
                .map(IRollerMode::find)
                .orElse(List.of());


        if (foundVillagers.isEmpty()) {
            MessageUtils.throwError("afk.enchant_roller.error.not_found_villager_or_lectern", Component.literal(modConfig.mode));

            stop();
            return;
        }

        list = foundVillagers;
        modConfigManager.setEntry();

        if (modConfig.autoBuy && !hasSufficientResources(player)) {
            MessageUtils.throwError("afk.enchant_roller.error.not_enough_emerald_or_book");

            stop();
            return;
        }

        MessageUtils.print("afk.enchant_roller.info.turnon");
    }

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
        if (modConfig.autoBuy) {
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
        if (!isEnabled || player == null || level == null || interactionManager == null) return;

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
        if (VillagerUtils.isCorrectVillagerProfession(villager)) {
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
        if (modConfig.preventAxeBreaking) {
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


    enum State{
        IDLE,
        INTERACT,
        BREAK,
        PLACE
    }
}


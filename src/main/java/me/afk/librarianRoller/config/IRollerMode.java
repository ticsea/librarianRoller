package me.afk.librarianRoller.config;

import me.afk.librarianRoller.dataModel.Librarians;
//? if >= 1.21.11 {

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
        //?} else {
/*import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
        *///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// implement this interface to create your custom RollerMode
// and call ModConfigManager.getRollerModeRegistry().add() method in mod init.
// if you want. you can override find() method to do what you want.
public interface IRollerMode {
    //? if > 1.21.1 {
    Predicate<Villager> isValidVillager = it -> (it.isAlive() && it.getVillagerData().profession().is(VillagerProfession.LIBRARIAN) && it.getVillagerXp() < 1);
    //?} else {
    /*Predicate<Villager> isValidVillager = it -> (it.isAlive() && it.getVillagerData().getProfession().equals(VillagerProfession.LIBRARIAN) && it.getVillagerXp() < 1);

    *///?}
    String getName();
    int getRequireCount();
    default boolean isCountValid(int count) {
        return count == getRequireCount();
    }

    double getRadius();

    default List<Librarians> find() {
        List<Librarians> list = new ArrayList<>();

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;

        if (level == null || player == null) {
            return list;
        }

        AABB aabb = player.getBoundingBox().inflate(getRadius());
        List<Villager> villagers = findVillagers(level, aabb, player);

        if (villagers.isEmpty()) {
            return list;
        }

        List<BlockPos> lecternsPos = findLecterns(villagers, player, level);

        if (lecternsPos.isEmpty()) {
            return list;
        }

        List<BlockHitResult> belowHitResults = findBelowHitResult(lecternsPos);

        if (belowHitResults.isEmpty()) {
            return list;
        }

        int requireCount = getRequireCount();
        for (int i = 0; i < requireCount; ++i) {
            list.add(new Librarians(villagers.get(i), lecternsPos.get(i), belowHitResults.get(i)));
        }

        return list;
    }

    default List<Villager> findVillagers(ClientLevel level, AABB aabb, LocalPlayer player) {
        List<Villager> list = new ArrayList<>();

        List<Villager> entitiesOfClass = level.getEntitiesOfClass(Villager.class, aabb, IRollerMode.isValidVillager)
                .stream()
                .sorted(Comparator.comparingDouble(v -> v.distanceToSqr(player)))
                .collect(Collectors.toList());

        while (entitiesOfClass.size() > getRequireCount()) {
            //? if FORGE {
            /*entitiesOfClass.remove(entitiesOfClass.size() - 1);
            *///?} else {
            entitiesOfClass.removeLast();
            //?}
        }

        if (!isCountValid(entitiesOfClass.size())) return list;

        list = entitiesOfClass;

        return list;
    }

    default List<BlockPos> findLecterns(List<Villager> villagers, LocalPlayer player, ClientLevel level) {
        List<BlockPos> list = new ArrayList<>();
        BlockPos playerFeetPos = player.blockPosition();

        villagers.forEach(it -> {
            BlockPos villagerFeetPos = it.blockPosition();
//            BlockPos vec = playerFeetPos.subtract(villagerFeetPos);
//            Direction cardinalDirection = getCardinalDirection(Vec3.atLowerCornerOf(vec));

            BlockPos lecternPos = null;
            List<BlockPos> direction = List.of(villagerFeetPos.north(), villagerFeetPos.east(), villagerFeetPos.south(), villagerFeetPos.west());


            for (BlockPos e : direction) {
                if (level.getBlockState(e).getBlock() instanceof LecternBlock){
                    lecternPos = e;
                    break;
                }
            }

            if (lecternPos == null) return;

            list.add(lecternPos);
        });

        return list;
    }

    default List<BlockHitResult> findBelowHitResult(List<BlockPos> lecterns) {
        List<BlockHitResult> list = new ArrayList<>();

        lecterns.forEach(it -> {
            list.add(new BlockHitResult(
                            new Vec3(
                                    it.getX(),
                                    it.getY(),
                                    it.getZ()
                            ), Direction.UP, it.below(), false
                    )
            );
        });

        return list;
    }

    private Direction getCardinalDirection(Vec3 vec) {
        double dx = vec.x();
        double dz = vec.z();

        // 判断哪个轴偏移更大
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}

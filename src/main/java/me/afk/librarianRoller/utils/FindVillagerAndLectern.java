package me.afk.librarianRoller.utils;

import me.afk.librarianRoller.VillagerAndLectern;
import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.config.RollerType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
        //? if >=1.21.11 {
import net.minecraft.world.entity.npc.villager.Villager;

        //?} else {
/*import net.minecraft.world.entity.npc.Villager;
*///?}
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FindVillagerAndLectern {

    public static List<VillagerAndLectern> findLecternAndVillager(LocalPlayer player, RollerType mode) {
        List<VillagerAndLectern> result = new ArrayList<>();
        Direction playerDirection = player.getDirection();
        var world = player.level();
        int x = player.blockPosition().getX();
        int y = player.blockPosition().getY();
        int z = player.blockPosition().getZ();

        int[][] villagersAndLecterns;

        // 1P
        if (mode == RollerType.V1) {
            int[][] single = new int[1][];
            switch (playerDirection) {
                case NORTH:
                    single[0] = new int[]{x, z - 1, x, z - 2};
                    break;
                case EAST:
                    single[0] = new int[]{x + 1, z, x + 2, z};
                    break;
                case SOUTH:
                    single[0] = new int[]{x, z + 1, x, z + 2};
                    break;
                case WEST:
                    single[0] = new int[]{x - 1, z, x - 2, z};
                    break;
                default:
                    single[0] = new int[0];
                    break;
            }
            villagersAndLecterns = single;
        }
        // 4P
        else if (mode == RollerType.V4) {
            villagersAndLecterns = new int[][]{
                    new int[]{x, z - 1, x, z - 2},
                    new int[]{x + 1, z, x + 2, z},
                    new int[]{x, z + 1, x, z + 2},
                    new int[]{x - 1, z, x - 2, z}
            };
        }
        // 6P
        else if (mode == RollerType.V6) {
            switch (playerDirection) {
                case NORTH:
                case SOUTH:
                    villagersAndLecterns = new int[][]{
                            new int[]{x - 1, z - 2, x - 2, z - 2},
                            new int[]{x - 1, z + 0, x - 2, z + 0},
                            new int[]{x - 1, z + 2, x - 2, z + 2},
                            new int[]{x + 1, z + 2, x + 2, z + 2},
                            new int[]{x + 1, z + 0, x + 2, z + 0},
                            new int[]{x + 1, z - 2, x + 2, z - 2}
                    };
                    break;
                case EAST:
                case WEST:
                    villagersAndLecterns = new int[][]{
                            new int[]{x - 2, z - 1, x - 2, z - 2},
                            new int[]{x + 0, z - 1, x + 0, z - 2},
                            new int[]{x + 2, z - 1, x + 2, z - 2},
                            new int[]{x + 2, z + 1, x + 2, z + 2},
                            new int[]{x + 0, z + 1, x + 0, z + 2},
                            new int[]{x - 2, z + 1, x - 2, z + 2}
                    };
                    break;
                default:
                    villagersAndLecterns = new int[0][];
                    break;
            }
        } else {
            villagersAndLecterns = new int[0][];
        }

        for (int[] dir : villagersAndLecterns) {
            if (dir.length < 4) continue;

            BlockPos lecternPos = new BlockPos(dir[0], y, dir[1]);
            BlockPos villagerPos = new BlockPos(dir[2], y, dir[3]);
            BlockHitResult lecternBelowHitResult = new BlockHitResult(
                    new Vec3(
                            lecternPos.getX(),
                            lecternPos.getY(),
                            lecternPos.getZ()
                    ), Direction.UP, lecternPos.below(), false
            );

            Villager villager = world.getEntitiesOfClass(
                    Villager.class,
                    new AABB(villagerPos)
            ).stream().filter(LivingEntity::isAlive).findFirst().orElse(null);

            if (villager == null) {
                MessageUtils.throwError("afk.enchant_roller.error.find_villager_failed", Component.literal(ModConfigManager.getConfig().mode.toString()));
                break;
            }

            var block = world.getBlockState(lecternPos).getBlock();
            if (!(block instanceof LecternBlock)) {
                MessageUtils.throwError("afk.enchant_roller.error.find_lectern_failed", Component.literal(ModConfigManager.getConfig().mode.toString()));
                break;
            }

            result.add(new VillagerAndLectern(lecternPos, villager, lecternBelowHitResult));
        }

        return result;
    }
}

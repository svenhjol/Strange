package svenhjol.strange.helper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;

public class ScrollHelper {
    public static final UUID ANY_UUID = UUID.fromString("0-0-0-0-1");

    // TODO: move to PosHelper
    public static boolean spawnMobNearPos(ServerWorld world, BlockPos pos, MobEntity mob, BiConsumer<MobEntity, BlockPos> onSpawn) {
        int range = 4;
        int tries = 8;
        Random random = world.random;
        List<BlockPos> validPositions = new ArrayList<>();
        int surface = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ());

        for (int y = surface; y < surface + range; y++) {
            for (int i = range; i > 1; --i) {
                for (int c = 1; c < tries; ++c) {
                    BlockPos checkPos = new BlockPos(pos.getX() + random.nextInt(i), y, pos.getZ() + random.nextInt(i));
                    BlockPos floor = checkPos.down();
                    BlockPos above = checkPos.up();
                    boolean areaIsValid = isLikeSolid(world, floor)
                        && isLikeAir(world, checkPos)
                        && isLikeAir(world, above);

                    if (areaIsValid)
                        validPositions.add(checkPos);

                    if (validPositions.size() > 2)
                        break;
                }
            }
        }

        if (validPositions.isEmpty()) {
            return false;
        } else {
            BlockPos spawnPos = validPositions.get(random.nextInt(validPositions.size()));
            mob.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);
            mob.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.TRIGGERED, null, null);
            world.spawnEntity(mob);
            onSpawn.accept(mob, spawnPos);
            return true;
        }
    }

    public static void clearWeather(ServerWorld world) {
        clearWeather(world, world.random.nextInt(12000) + 3600);
    }

    public static void clearWeather(ServerWorld world, int duration) {
        world.setWeather(duration, 0, false, false);
    }

    public static void stormyWeather(ServerWorld world) {
        stormyWeather(world, world.random.nextInt(12000) + 3600);
    }

    public static void stormyWeather(ServerWorld world, int duration) {
        world.setWeather(0, duration, true, true);
    }

    public static boolean isLikeSolid(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return isSolid(world, pos) || state.getMaterial() == Material.LEAVES || state.getMaterial() == Material.SNOW_LAYER || state.getMaterial() == Material.ORGANIC_PRODUCT || state.getMaterial() == Material.PLANT;
    }

    public static boolean isLikeAir(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.isOpaque() || state.getMaterial() == Material.WATER || state.getMaterial() == Material.SNOW_LAYER || state.getMaterial() == Material.PLANT || state.getMaterial() == Material.LEAVES || state.getMaterial() == Material.ORGANIC_PRODUCT;
    }

    public static boolean isSolid(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isOpaque() && !world.isAir(pos) && !state.getMaterial().isLiquid();
    }

}

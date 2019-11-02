package svenhjol.strange.outerlands.module;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;

import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.OUTERLANDS, hasSubscriptions = true)
public class Outerlands extends MesonModule
{
    @Config(name = "Threshold", description = "X or Z axis values greater than this value are considered 'outer lands'")
    public static int threshold = 12000000;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        IWorld world = event.getWorld();
        if (world == null) return;

        // don't let outer area be greater than worldborder
        threshold = Math.min(threshold, getMaxDistance(world));
    }

    public static BlockPos getInnerPos(World world, Random rand)
    {
        int x = rand.nextInt(threshold * 2) - threshold;
        int z = rand.nextInt(threshold * 2) - threshold;
        return new BlockPos(x, 0, z);
    }

    public static BlockPos getOuterPos(World world, Random rand)
    {
        int x = rand.nextInt(world.getWorldBorder().getSize() - threshold) + threshold;
        int z = rand.nextInt(world.getWorldBorder().getSize() - threshold) + threshold;

        x = rand.nextFloat() < 0.5 ? x : -x;
        z = rand.nextFloat() < 0.5 ? z : -z;

        return new BlockPos(x, 0, z);
    }

    public static int getMaxDistance(IWorld world)
    {
        return world.getWorldBorder().getSize() - 10000;
    }

    public static float getScaledMultiplier(IWorld world, BlockPos pos)
    {
        if (isInnerPos(pos)) return 1.0F;

        float dist = Math.max(Math.abs(pos.getX()), Math.abs(pos.getZ())) - threshold;
        float max = getMaxDistance(world) - threshold;

        return 1.0F + (dist / max);
    }

    public static boolean isInnerPos(BlockPos pos)
    {
        return Math.abs(pos.getX()) <= threshold || Math.abs(pos.getZ()) <= threshold;
    }

    public static boolean isOuterPos(BlockPos pos)
    {
        return Math.abs(pos.getX()) > threshold || Math.abs(pos.getZ()) > threshold;
    }
}

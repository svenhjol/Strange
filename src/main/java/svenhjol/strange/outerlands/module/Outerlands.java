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
    @Config(name = "Distance", description = "X or Z axis values greater than this value are considered 'outer lands'")
    public static int distance = 12000000;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        IWorld world = event.getWorld();
        if (world == null) return;

        // don't let outer threshold be greater than worldborder
        distance = Math.min(distance, world.getWorldBorder().getSize() - 10000);
    }

    public static BlockPos getInnerPos(World world, Random rand)
    {
        int x = rand.nextInt(distance * 2) - distance;
        int z = rand.nextInt(distance * 2) - distance;
        return new BlockPos(x, 0, z);
    }

    public static BlockPos getOuterPos(World world, Random rand)
    {
        int x = rand.nextInt(world.getWorldBorder().getSize() - distance) + distance;
        int z = rand.nextInt(world.getWorldBorder().getSize() - distance) + distance;

        x = rand.nextFloat() < 0.5 ? x : -x;
        z = rand.nextFloat() < 0.5 ? z : -z;

        return new BlockPos(x, 0, z);
    }

    public static boolean isOuterPos(BlockPos pos)
    {
        return pos.getX() > distance || pos.getZ() > distance;
    }
}

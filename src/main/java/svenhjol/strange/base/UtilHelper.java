package svenhjol.strange.base;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.Objects;

public class UtilHelper
{
    public static String formatBlockPos(BlockPos pos)
    {
        if (pos == null) return "";
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    public static String getBiomeName(Biome biome)
    {
        if (biome == null) return "";
        return Objects.requireNonNull(biome.getRegistryName()).getPath();
    }
}

package svenhjol.strange.ambience.client.iface;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public interface IBiome extends IAmbientSounds
{
    @Override
    default boolean isValid()
    {
        if (getWorld() == null) return false;
        BlockPos pos = getPlayer().getPosition();
        Biome biome = getWorld().getBiome(pos);
        if (biome == null) return false;

        return biome.getCategory().equals(getBiomeCategory());
    }

    Biome.Category getBiomeCategory();
}

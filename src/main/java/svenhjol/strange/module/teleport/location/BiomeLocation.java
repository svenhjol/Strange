package svenhjol.strange.module.teleport.location;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class BiomeLocation extends BaseLocation {
    public BiomeLocation(ResourceLocation location, float difficulty) {
        super(location, difficulty);
    }

    @Nullable
    public BlockPos getTarget(ServerLevel level, BlockPos startPos, Random random) {
        int maxDistance = 5000; // TODO: config
        int xdist = -maxDistance + random.nextInt(maxDistance * 2);
        int zdist = -maxDistance + random.nextInt(maxDistance * 2);
        BlockPos destPos = checkBounds(level, startPos.offset(xdist, 0, zdist));

        // TODO check this works with modded biomes
        Optional<Biome> biome = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOptional(location);

        if (biome.isEmpty()) {
            LogHelper.warn(this.getClass(), "Could not find biome in registry of type: " + location);
            return null;
        }

        LogHelper.debug(this.getClass(), "Trying to locate biome in the world: " + location);
        BlockPos foundPos = level.findNearestBiome(biome.get(), destPos, 6400, 8); // ints stolen from LocateBiomeCommand

        if (foundPos == null) {
            LogHelper.warn(this.getClass(), "Could not locate biome: " + location);
            return null;
        }

        return WorldHelper.addRandomOffset(foundPos, random, 6, 12);
    }
}

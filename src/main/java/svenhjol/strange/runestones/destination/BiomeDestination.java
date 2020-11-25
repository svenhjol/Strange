package svenhjol.strange.runestones.destination;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import svenhjol.charm.Charm;
import svenhjol.charm.base.helper.PosHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class BiomeDestination extends Destination {
    public BiomeDestination(Identifier location, float weight) {
        super(location, weight);
    }

    @Nullable
    public BlockPos getDestination(ServerWorld world, BlockPos startPos, int maxDistance, Random random, @Nullable ServerPlayerEntity player) {

        BlockPos loadedPos = tryLoad(world, startPos);
        if (loadedPos != null)
            return loadedPos;

        int xdist = -maxDistance + random.nextInt(maxDistance *2);
        int zdist = -maxDistance + random.nextInt(maxDistance *2);
        BlockPos destPos = checkBounds(world, startPos.add(xdist, 0, zdist));

        BlockPos foundPos;

        if (isSpawnPoint()) {
            foundPos = world.getSpawnPos();
        } else {

            // TODO check this works with modded biomes
            Optional<Biome> biome = world.getRegistryManager().get(Registry.BIOME_KEY).getOrEmpty(location);

            if (!biome.isPresent()) {
                Charm.LOG.warn("Could not find biome in registry of type: " + location);
                return null;
            }

            Charm.LOG.debug("Trying to locate biome in the world: " + location);
            foundPos = world.locateBiome(biome.get(), destPos, 6400, 8); // ints stolen from LocateBiomeCommand
        }

        if (foundPos == null) {
            Charm.LOG.warn("Could not locate biome: " + location);
            return null;
        }

        foundPos = PosHelper.addRandomOffset(foundPos, random, 8, 16);
        store(world, startPos, foundPos, player);

        return foundPos;
    }
}

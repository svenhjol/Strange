package svenhjol.strange.runestones.destination;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.charm.Charm;
import svenhjol.charm.base.helper.PosHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class StructureDestination extends Destination {
    public StructureDestination(Identifier location, float weight) {
        super(location, weight);
    }

    @Nullable
    public BlockPos getDestination(ServerWorld world, BlockPos startPos, int maxDistance, Random random, @Nullable ServerPlayerEntity player) {
        BlockPos loadedPos = tryLoad(world, startPos);
        if (loadedPos != null)
            return loadedPos;

        int xdist = -maxDistance + random.nextInt(maxDistance * 2);
        int zdist = -maxDistance + random.nextInt(maxDistance * 2);
        BlockPos destPos = checkBounds(world, startPos.add(xdist, 0, zdist));

        BlockPos foundPos;

        if (isSpawnPoint()) {
            foundPos = world.getSpawnPos();
        } else {
            StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(location);

            if (structureFeature == null) {
                Charm.LOG.warn("Could not find structure in registry of type: " + location);
                return null;
            }

            Charm.LOG.debug("Trying to locate structure in the world: " + location);
            foundPos = world.locateStructure(structureFeature, destPos, 1000, false);
        }

        if (foundPos == null) {
            Charm.LOG.warn("Could not locate structure: " + location);
            return null;
        }

        foundPos = PosHelper.addRandomOffset(foundPos, random, 500, 1500);
        store(world, startPos, foundPos, player);

        return foundPos;
    }
}

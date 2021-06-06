package svenhjol.strange.module.runestones.destination;

import svenhjol.charm.Charm;
import svenhjol.charm.helper.PosHelper;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import java.util.Random;

public class StructureDestination extends BaseDestination {
    public StructureDestination(ResourceLocation location, float weight) {
        super(location, weight);
    }

    @Nullable
    public BlockPos getDestination(ServerLevel world, BlockPos startPos, int maxDistance, Random random, @Nullable ServerPlayer player) {
        BlockPos loadedPos = tryLoad(world, startPos);
        if (loadedPos != null)
            return loadedPos;

        int xdist = -maxDistance + random.nextInt(maxDistance * 2);
        int zdist = -maxDistance + random.nextInt(maxDistance * 2);
        BlockPos destPos = checkBounds(world, startPos.offset(xdist, 0, zdist));

        BlockPos foundPos;

        if (isSpawnPoint()) {
            foundPos = world.getSharedSpawnPos();
        } else {
            StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(location);

            if (structureFeature == null) {
                Charm.LOG.warn("Could not find structure in registry of type: " + location);
                return null;
            }

            Charm.LOG.debug("Trying to locate structure in the world: " + location);
            foundPos = world.findNearestMapFeature(structureFeature, destPos, 1000, false);
        }

        if (foundPos == null) {
            Charm.LOG.warn("Could not locate structure: " + location);
            return null;
        }

        foundPos = PosHelper.addRandomOffset(foundPos, random, 6, 12);
        store(world, startPos, foundPos, player);

        return foundPos;
    }
}

package svenhjol.strange.destination;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.meson.Meson;
import svenhjol.strange.helper.RunestoneHelper;
import svenhjol.strange.module.Runestones;

import javax.annotation.Nullable;
import java.util.Random;

public class StructureDestination extends Destination {
    public StructureDestination(Identifier location, float weight) {
        super(location, weight);
    }

    @Nullable
    public BlockPos getDestination(ServerWorld world, BlockPos runePos, Random random, @Nullable ServerPlayerEntity player) {
        int maxDistance = Runestones.maxDistance;

        BlockPos loadedPos = tryLoad(world, runePos);
        if (loadedPos != null)
            return loadedPos;

        int xdist = -maxDistance + random.nextInt(maxDistance *2);
        int zdist = -maxDistance + random.nextInt(maxDistance *2);
        BlockPos destPos = checkBounds(world, runePos.add(xdist, 0, zdist));

        BlockPos foundPos;

        if (isSpawnPoint()) {
            foundPos = world.getSpawnPos();
        } else {

            // TODO check this works with modded structures
            String structureId = location.getNamespace().equals("minecraft") ? location.getPath() : location.toString();
            StructureFeature<?> structureFeature = StructureFeature.STRUCTURES.get(structureId);

            if (structureFeature == null) {
                Meson.LOG.warn("Could not find structure in registry of type: " + location);
                return null;
            }

            Meson.LOG.debug("Trying to locate structure in the world: " + location);
            foundPos = world.locateStructure(structureFeature, destPos, 1000, false);
        }

        if (foundPos == null) {
            Meson.LOG.warn("Could not locate structure: " + location);
            return null;
        }

        foundPos = RunestoneHelper.addRandomOffset(foundPos, random, 8);
        store(world, runePos, foundPos, player);

        return foundPos;
    }
}

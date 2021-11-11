package svenhjol.strange.module.teleport.location;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class StructureLocation extends BaseLocation {
    public StructureLocation(ResourceLocation location, float difficulty) {
        super(location, difficulty);
    }

    @Nullable
    public BlockPos getTarget(ServerLevel level, BlockPos startPos, Random random) {
        int maxDistance = 5000; // TODO: config
        int xdist = -maxDistance + random.nextInt(maxDistance * 2);
        int zdist = -maxDistance + random.nextInt(maxDistance * 2);
        BlockPos destPos = checkBounds(level, startPos.offset(xdist, 0, zdist));
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(location);

        if (structureFeature == null) {
            LogHelper.warn(this.getClass(), "Could not find structure in registry of type: " + location);
            return null;
        }

        LogHelper.debug(this.getClass(), "Trying to locate structure in the world: " + location);
        BlockPos foundPos = level.findNearestMapFeature(structureFeature, destPos, 1000, false);

        if (foundPos == null) {
            LogHelper.warn(this.getClass(), "Could not locate structure: " + location);
            return null;
        }

        return WorldHelper.addRandomOffset(foundPos, random, 6, 12);
    }
}

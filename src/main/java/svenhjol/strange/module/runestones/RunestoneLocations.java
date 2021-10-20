package svenhjol.strange.module.runestones;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.location.BaseLocation;
import svenhjol.strange.module.runestones.location.BiomeLocation;
import svenhjol.strange.module.runestones.location.StructureLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunestoneLocations {
    public static final String UNKNOWN_CLUE = "unknown";
    public static final ResourceLocation SPAWN = new ResourceLocation(Strange.MOD_ID, "spawn_point");

    public static void init() {
        // assemble map of dimension -> locations first
        Map<ResourceLocation, List<ResourceLocation>> dimensionLocations = new HashMap<>();

        for (int i = 0; i < Runestones.configLocations.size(); i++) {

            List<String> split = StringHelper.splitConfigEntry(Runestones.configLocations.get(i));
            if (split.size() != 2) continue;

            ResourceLocation dimensionId = new ResourceLocation(split.get(0));
            ResourceLocation locationId = new ResourceLocation(split.get(1));
            dimensionLocations.computeIfAbsent(dimensionId, a -> new ArrayList<>())
                .add(locationId);
        }

        // iterate through each dimension, setting the difficulty for each location by its position in the list
        dimensionLocations.forEach((dimensionId, locations) -> {
            for (int i = 0; i < locations.size(); i++) {
                float difficulty = 0.0F + (i / (float)locations.size());
                ResourceLocation locationId = locations.get(i);
                BaseLocation location;
                String type;

                if (WorldHelper.isBiome(locationId)) {
                    type = "biome";
                    location = new BiomeLocation(locationId, difficulty);
                } else if (WorldHelper.isStructure(locationId)) {
                    type = "structure";
                    location = new StructureLocation(locationId, difficulty);
                } else {
                    LogHelper.debug(RunestoneLocations.class, "Location " + locationId + " is not a structure or biome, ignoring as runestone destination");
                    continue;
                }

                Runestones.DIMENSION_LOCATIONS.computeIfAbsent(dimensionId, a -> new ArrayList<>()).add(location);
                LogHelper.debug(RunestoneLocations.class, "Added " + type + " " + locationId + " to dimension " + dimensionId + " with difficulty " + difficulty);
            }
        });
    }

}

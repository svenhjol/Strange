package svenhjol.strange.module.runestones;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.Strange;

import java.util.*;

public class RunestoneLocations {
    public static final String UNKNOWN_CLUE = "unknown";
    public static final ResourceLocation SPAWN = new ResourceLocation(Strange.MOD_ID, "spawn_point");

    public static void init() {
        // assemble map of dimension -> destinations first
        Map<ResourceLocation, List<ResourceLocation>> dimensionDestinations = new HashMap<>();

        for (int i = 0; i < Runestones.configDestinations.size(); i++) {
            List<String> split = StringHelper.splitConfigEntry(Runestones.configDestinations.get(i));
            if (split.size() != 2) continue;

            ResourceLocation dimensionId = new ResourceLocation(split.get(0));
            ResourceLocation destinationId = new ResourceLocation(split.get(1));
            dimensionDestinations.computeIfAbsent(dimensionId, a -> new LinkedList<>()).add(destinationId);
        }

        dimensionDestinations.forEach((dimensionId, destinations) -> {
            for (ResourceLocation destinationId : destinations) {
                Runestones.DESTINATIONS.computeIfAbsent(dimensionId, a -> new ArrayList<>()).add(destinationId);
                LogHelper.debug(RunestoneLocations.class, "Added " + destinationId + " to dimension " + dimensionId);
            }
        });
    }
}

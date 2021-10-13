package svenhjol.strange.module.runestones;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.runestones.location.BiomeLocation;
import svenhjol.strange.module.runestones.location.StructureLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RunestoneLocations {
    public static void create() {
        // add all structures from config file
        for (int i = 0; i < Runestones.configStructures.size(); i++) {
            List<String> split = splitConfigEntry(Runestones.configStructures.get(i));
            if (split.size() != 2) continue;

            ResourceLocation dimensionId = new ResourceLocation(split.get(0));
            ResourceLocation structureId = new ResourceLocation(split.get(1));
            if (!isValidStructure(structureId)) continue;

            float weight = 1.0F - (i / (float) Runestones.configStructures.size());
            Runestones.AVAILABLE_LOCATIONS.computeIfAbsent(dimensionId, a -> new ArrayList<>())
                .add(new StructureLocation(structureId, weight));
        }

        // add all biomes from config file
        for (int i = 0; i < Runestones.configBiomes.size(); i++) {
            List<String> split = splitConfigEntry(Runestones.configBiomes.get(i));
            if (split.size() != 2) continue;

            ResourceLocation dimensionId = new ResourceLocation(split.get(0));
            ResourceLocation biomeId = new ResourceLocation(split.get(1));
            if (!isValidBiome(biomeId)) continue;

            float weight = 1.0F - (i / (float) Runestones.configBiomes.size());
            Runestones.AVAILABLE_LOCATIONS.computeIfAbsent(dimensionId, a -> new ArrayList<>())
                .add(new BiomeLocation(biomeId, weight));
        }
    }


    private static List<String> splitConfigEntry(String entry) {
        return Arrays.stream(entry.split("->")).map(s -> s.trim().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }

    private static boolean isValidStructure(ResourceLocation structureId) {
        if (Registry.STRUCTURE_FEATURE.get(structureId) == null) {
            LogHelper.debug(RunestoneLocations.class, "Could not find structure " + structureId + ", ignoring as runestone destination");
            return false;
        }
        return true;
    }

    private static boolean isValidBiome(ResourceLocation biomeId) {
        if (BuiltinRegistries.BIOME.get(biomeId) == null) {
            LogHelper.debug(RunestoneLocations.class, "Could not find biome " + biomeId + ", ignoring as runestone destination");
            return false;
        }
        return true;
    }
}

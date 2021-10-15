package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.Destination;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.knowledge.KnowledgeHelper;
import svenhjol.strange.module.runestones.location.BaseLocation;
import svenhjol.strange.module.runestones.location.BiomeLocation;
import svenhjol.strange.module.runestones.location.StructureLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class RunestoneLocations {
    public static final ResourceLocation SPAWN = new ResourceLocation(Strange.MOD_ID, "spawn");

    public static void init() {
        // assemble map of dimension -> locations first
        Map<ResourceLocation, List<ResourceLocation>> dimensionLocations = new HashMap<>();

        for (int i = 0; i < Runestones.configLocations.size(); i++) {

            List<String> split = splitConfigEntry(Runestones.configLocations.get(i));
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

                if (isBiome(locationId)) {
                    type = "biome";
                    location = new BiomeLocation(locationId, difficulty);
                } else if (isStructure(locationId)) {
                    type = "structure";
                    location = new StructureLocation(locationId, difficulty);
                } else {
                    LogHelper.debug(RunestoneLocations.class, "Location " + locationId + " is not a structure or biome, ignoring as runestone destination");
                    continue;
                }

                Runestones.AVAILABLE_LOCATIONS.computeIfAbsent(dimensionId, a -> new ArrayList<>()).add(location);
                LogHelper.debug(RunestoneLocations.class, "Added " + type + " " + locationId + " to dimension " + dimensionId + " with difficulty " + difficulty);
            }
        });
    }

    public static Optional<Destination> createSpawnDestination() {
        // check if it's already been created
        KnowledgeData knowledgeData = Knowledge.getSavedData().orElseThrow();
        String spawnRune = String.valueOf(knowledgeData.SPAWN_RUNE);

        if (knowledgeData.hasDestination(spawnRune)) {
            return knowledgeData.getDestination(spawnRune);
        }

        Destination destination = new Destination(spawnRune);
        destination.location = SPAWN;
        destination.items = Arrays.asList(new ItemStack(Items.WHEAT_SEEDS)); // TODO: testdata

        knowledgeData.updateDestination(spawnRune, destination);
        return Optional.of(destination);
    }

    public static Optional<Destination> createDestination(ResourceLocation dimension, Random random, float difficulty, float decay, @Nullable ResourceLocation location) {
        if (location != null) {
            if (location == SPAWN) {
                return createSpawnDestination();
            }
        } else {
            location = tryFetchLocation(dimension, random, difficulty);
            if (location == null) {
                return Optional.empty();
            }
        }

        String runes = tryGenerateUniqueId(random, difficulty);
        if (runes == null) {
            return Optional.empty();
        }

        Destination destination = new Destination(runes);
        destination.difficulty = difficulty;
        destination.decay = decay;
        destination.pos = BlockPos.ZERO;
        destination.dimension = dimension;
        destination.location = location;
        destination.items = Arrays.asList(new ItemStack(Items.DIAMOND)); // TODO: testdata

        return tryUpdateDestination(runes, destination) ? Optional.of(destination) : Optional.empty();
    }

    private static boolean tryUpdateDestination(String runes, Destination destination) {
        Optional<KnowledgeData> optData = Knowledge.getSavedData();
        if (optData.isEmpty()) {
            LogHelper.error(RunestoneLocations.class, "Could not access knowledge data");
            return false;
        }

        optData.get().updateDestination(runes, destination);
        return true;
    }

    @Nullable
    private static ResourceLocation tryFetchLocation(ResourceLocation dimension, Random random, float difficulty) {
        // fetch a location using the difficulty
        if (!Runestones.AVAILABLE_LOCATIONS.containsKey(dimension)) {
            LogHelper.debug(RunestoneLocations.class, "No locations available for this dimension, giving up");
            return null;
        }

        List<BaseLocation> locations = Runestones.AVAILABLE_LOCATIONS.get(dimension).stream()
            .filter(l -> l.getDifficulty() <= difficulty && l.getDifficulty() > difficulty - 0.1F)
            .collect(Collectors.toList());

        if (locations.isEmpty()) {
            LogHelper.debug(RunestoneLocations.class, "No locations found for this difficulty, giving up");
        }

        Collections.shuffle(locations, random);
        return locations.get(0).getLocation();
    }

    @Nullable
    private static String tryGenerateUniqueId(Random random, float difficulty) {
        KnowledgeData knowledgeData = Knowledge.getSavedData().orElseThrow();
        int tries = 0;
        int maxTries = 20;
        boolean foundUniqueRunes = false;
        String runes = "";

        // keep trying to find a unique rune string for this destination
        while (!foundUniqueRunes && tries < maxTries) {
            runes = KnowledgeHelper.generateDestinationString(random, difficulty + (tries * 0.05F));
            foundUniqueRunes = !knowledgeData.hasDestination(runes);
            ++tries;
        }

        if (!foundUniqueRunes) {
            LogHelper.debug(RunestoneLocations.class, "Could not calculate unique rune string for this destination, giving up");
            return null;
        }

        return knowledgeData.DESTINATION_RUNE + runes;
    }

    private static List<String> splitConfigEntry(String entry) {
        return Arrays.stream(entry.split("->")).map(s -> s.trim().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }

    private static boolean isStructure(ResourceLocation structureId) {
        return Registry.STRUCTURE_FEATURE.get(structureId) != null;
    }

    private static boolean isBiome(ResourceLocation biomeId) {
        return BuiltinRegistries.BIOME.get(biomeId) != null;
    }
}

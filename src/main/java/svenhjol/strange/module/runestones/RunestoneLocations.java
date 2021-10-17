package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.helper.WorldHelper;
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

        Random random = KnowledgeHelper.getRandom();
        Destination destination = new Destination(spawnRune);
        destination.location = SPAWN;
        destination.clue = getClue(SPAWN, random);
        destination.items = getItems(0.0F, random);

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
        destination.clue = getClue(location, random);
        destination.items = getItems(difficulty, random);

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
            LogHelper.debug(RunestoneLocations.class, "No locations found for this difficulty (" + difficulty + "), defaulting to first available");
            locations.add(Runestones.AVAILABLE_LOCATIONS.get(dimension).get(0));
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
            runes = KnowledgeHelper.generateDestinationRunes(random, difficulty + (tries * 0.05F));
            foundUniqueRunes = !knowledgeData.hasDestination(runes);
            ++tries;
        }

        if (!foundUniqueRunes) {
            LogHelper.debug(RunestoneLocations.class, "Could not calculate unique rune string for this destination, giving up");
            return null;
        }

        return knowledgeData.DESTINATION_RUNE + runes;
    }

    private static List<Item> getItems(float difficulty, Random random) {
        List<Item> items;

        int tier = Math.round(Runestones.TIERS * difficulty);
        if (Runestones.items.containsKey(tier) && !Runestones.items.get(tier).isEmpty()) {
            List<Item> tierItems = Runestones.items.get(tier);
            Collections.shuffle(tierItems, random);
            items = tierItems.subList(0, Math.min(tierItems.size(), 4));
        } else {
            items = List.of(Items.ENDER_PEARL);
        }

        return items;
    }

    private static String getClue(ResourceLocation location, Random random) {
        if (Runestones.clues.containsKey(location) && !Runestones.clues.get(location).isEmpty()) {
            List<String> clues = Runestones.clues.get(location);
            return clues.get(random.nextInt(clues.size()));
        }

        return UNKNOWN_CLUE;
    }
}

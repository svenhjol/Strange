package svenhjol.strange.module.runestones;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.knowledge.Destination;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.knowledge.KnowledgeHelper;
import svenhjol.strange.module.runestones.location.BaseLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class RunestoneHelper {
    public static Item DEFAULT_ITEM = Items.ENDER_PEARL;

    public static Item getItem(ResourceLocation dimension, float difficulty, Random random) {
        if (!Runestones.dimensionItems.containsKey(dimension)) {
            return DEFAULT_ITEM;
        }

        Map<Integer, List<Item>> items = Runestones.dimensionItems.get(dimension);
        int tier = Math.round(Runestones.TIERS * difficulty);

        if (items.containsKey(tier) && !items.get(tier).isEmpty()) {
            List<Item> tierItems = items.get(tier);

            tierItems.sort((i1, i2) -> {
                if (i1.hashCode() == i2.hashCode()) return 0;
                return i1.hashCode() < i2.hashCode() ? -1 : 1;
            });

            return tierItems.get(random.nextInt(tierItems.size()));
        } else {
            return DEFAULT_ITEM;
        }
    }

    public static List<Item> getItems(ResourceLocation dimension, float difficulty, Random random) {
        if (!Runestones.dimensionItems.containsKey(dimension)) {
            return List.of(Items.ENDER_PEARL);
        }

        Map<Integer, List<Item>> items = Runestones.dimensionItems.get(dimension);
        int tier = Math.round(Runestones.TIERS * difficulty);

        if (items.containsKey(tier) && !items.get(tier).isEmpty()) {
            List<Item> tierItems = new LinkedList<>(items.get(tier));
            Collections.shuffle(tierItems, random);
            return tierItems.subList(0, Math.min(tierItems.size(), Runestones.MAX_ITEMS)).stream().distinct().collect(Collectors.toList());
        } else {
            return List.of(Items.ENDER_PEARL);
        }
    }

    public static String getClue(ResourceLocation location, Random random) {
        if (Runestones.clues.containsKey(location) && !Runestones.clues.get(location).isEmpty()) {
            List<String> clues = Runestones.clues.get(location);
            return clues.get(random.nextInt(clues.size()));
        }

        return RunestoneLocations.UNKNOWN_CLUE;
    }

    public static Optional<ResourceLocation> tryFetchLocationByDifficulty(ResourceLocation dimension, Random random, float difficulty) {
        // fetch a location using the difficulty
        if (!Runestones.DIMENSION_LOCATIONS.containsKey(dimension)) {
            LogHelper.debug(RunestoneHelper.class, "No locations available for this dimension, giving up");
            return Optional.empty();
        }

        List<BaseLocation> dimensionRunestones = Runestones.DIMENSION_LOCATIONS.get(dimension);
        float size = 1F / dimensionRunestones.size();

        List<BaseLocation> locations = dimensionRunestones.stream()
            .filter(l -> l.getDifficulty() <= difficulty + size && l.getDifficulty() > difficulty - size)
            .collect(Collectors.toList());

        if (locations.isEmpty()) {
            LogHelper.debug(RunestoneHelper.class, "No locations found for this difficulty (" + difficulty + "), defaulting to first available");
            locations.add(Runestones.DIMENSION_LOCATIONS.get(dimension).get(0));
        }

        Collections.shuffle(locations, random);
        return Optional.of(locations.get(0).getLocation());
    }

    public static Optional<Destination> getOrCreateDestination(ResourceLocation dimension, Random random, float difficulty, float decay, @Nullable ResourceLocation location) {
        KnowledgeData knowledge = Knowledge.getSavedData().orElseThrow();

        if (location != null) {
            if (location.equals(RunestoneLocations.SPAWN)) {
                // does it exist?
                Optional<Destination> optSpawn = knowledge.specials.values().stream().filter(d -> d.getLocation().equals(RunestoneLocations.SPAWN)).findFirst();
                if (optSpawn.isPresent()) {
                    // generate items for it
                    Destination spawn = optSpawn.get();
                    return Optional.of(spawn);
                }

                // generate a new set of runes for it, check if they already exist
                String runes = KnowledgeHelper.tryGenerateUniqueId(knowledge.specials, random, difficulty, 1, 1).orElseThrow();

                Destination destination = new Destination(runes, RunestoneLocations.SPAWN);

                knowledge.specials.add(runes, destination);
                knowledge.setDirty();
                return Optional.of(destination);
            }
        } else {
            Optional<ResourceLocation> optLocation = tryFetchLocationByDifficulty(dimension, random, difficulty);
            if (optLocation.isEmpty()) {
                return Optional.empty();
            }

            location = optLocation.get();
        }

        String runes = KnowledgeHelper.tryGenerateUniqueId(knowledge.destinations, random, difficulty, Knowledge.MIN_LENGTH, Knowledge.MAX_LENGTH).orElseThrow();

        Destination destination = new Destination(runes, location);
        destination.setDifficulty(difficulty);
        destination.setDecay(decay);
        destination.setDimension(dimension);

        knowledge.destinations.add(runes, destination);
        knowledge.setDirty();

        return knowledge.destinations.get(runes);
    }
}

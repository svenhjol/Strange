package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class RunestoneHelper {
    public static Item getItem(ResourceLocation dimension, float difficulty, Random random) {
        Item item;

        // dimension doesn't do anything yet

        int tier = Math.round(Runestones.TIERS * difficulty);
        if (Runestones.items.containsKey(tier) && !Runestones.items.get(tier).isEmpty()) {
            List<Item> tierItems = Runestones.items.get(tier);

            tierItems.sort((i1, i2) -> {
                if (i1.hashCode() == i2.hashCode()) return 0;
                return i1.hashCode() < i2.hashCode() ? -1 : 1;
            });

            item = tierItems.get(random.nextInt(tierItems.size()));
        } else {
            item = Items.ENDER_PEARL;
        }

        return item;
    }

    public static List<Item> getItems(ResourceLocation dimension, float difficulty, Random random) {
        List<Item> items;

        // dimension doesn't do anything yet

        int tier = Math.round(Runestones.TIERS * difficulty);
        if (Runestones.items.containsKey(tier) && !Runestones.items.get(tier).isEmpty()) {
            List<Item> tierItems = Runestones.items.get(tier);
            Collections.shuffle(tierItems, random);
            items = tierItems.subList(0, Math.min(tierItems.size(), Runestones.MAX_ITEMS));
        } else {
            items = List.of(Items.ENDER_PEARL);
        }

        return items;
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

        List<BaseLocation> locations = Runestones.DIMENSION_LOCATIONS.get(dimension).stream()
            .filter(l -> l.getDifficulty() <= difficulty && l.getDifficulty() > difficulty - 0.1F)
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
                Optional<Destination> optSpawn = knowledge.specials.values().stream().filter(d -> d.location.equals(RunestoneLocations.SPAWN)).findFirst();
                if (optSpawn.isPresent()) {
                    return optSpawn;
                }

                // generate a new set of runes for it, check if they already exist
                String runes = KnowledgeHelper.tryGenerateUniqueId(knowledge.specials, random, difficulty, 1, 1).orElseThrow();

                Destination destination = new Destination(runes);
                destination.pos = BlockPos.ZERO;
                destination.location = RunestoneLocations.SPAWN;
                destination.clue = getClue(RunestoneLocations.SPAWN, random);
                destination.items = getItems(dimension, 0.0F, random);

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

        Destination destination = new Destination(runes);
        destination.difficulty = difficulty;
        destination.decay = decay;
        destination.pos = BlockPos.ZERO;
        destination.dimension = dimension;
        destination.location = location;
        destination.clue = getClue(location, random);
        destination.items = getItems(dimension, difficulty, random);

        knowledge.destinations.add(runes, destination);
        knowledge.setDirty();

        return knowledge.destinations.get(runes);
    }
}

package svenhjol.strange.module.runestones.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Runes;
import svenhjol.strange.module.runes.Tier;
import svenhjol.strange.module.runestones.Runestones;

import java.util.*;
import java.util.stream.Collectors;

public class RunestoneHelper {
    public static List<Item> getItems(ResourceLocation dimension, String runes) {
        if (!Runestones.ITEMS.containsKey(dimension)) {

            // When there are no items for a given dimension then we use a default list.
            LogHelper.debug(RunestoneHelper.class, "Could not find items for dimension `" + dimension + "`, defaulting.");
            return Runestones.DEFAULT_ITEMS;

        }

        // Create RNG for this runephrase. This guarantees we get the same selection when we shuffle the possible items.
        long seed = RuneHelper.seed(runes);
        Random random = new Random(seed);
        Map<Tier, List<Item>> items = Runestones.ITEMS.get(dimension);

        // Calculate the "tier" of items that we will show for this location.
        // This is determined by the length of the rune phrase.
        int len = Math.min(Runes.NUM_RUNES, runes.length());
        int val = Math.max(1, Math.min(Tier.size(), Math.round(Tier.size() * (len / (float)Runes.NUM_RUNES))));
        Tier tier = Tier.byLevel(val);

        if (items.containsKey(tier) && !items.get(tier).isEmpty()) {

            // Shuffle items according to the seed we generated for this rune phrase and return a selection of items.
            // The maximum number of items returned is limited to the MAX_ITEMS constant.
            List<Item> tierItems = new LinkedList<>(items.get(tier));
            Collections.shuffle(tierItems, random);
            return tierItems.subList(0, Math.min(tierItems.size(), Runestones.MAX_ITEMS))
                .stream()
                .distinct()
                .collect(Collectors.toList());

        } else {

            // When items fail to be fetched fallback to the default list.
            return Runestones.DEFAULT_ITEMS;

        }
    }

    /**
     * Fetch all the potential items for this rune phrase and select the requires one (index = 0).
     * This is useful for displays that just show the required item rather than all potential items.
     */
    public static Item getItem(ResourceLocation dimension, String runes) {
        List<Item> items = getItems(dimension, runes);
        return items.get(0);
    }

    public static String getClue(ResourceLocation location, Random random) {
        if (Runestones.CLUES.containsKey(location) && !Runestones.CLUES.get(location).isEmpty()) {
            List<String> clues = Runestones.CLUES.get(location);
            return clues.get(random.nextInt(clues.size()));
        }

        return Runestones.UNKNOWN_CLUE;
    }

//    public static Optional<ResourceLocation> tryFetchDestinationByDifficulty(ResourceLocation dimension, Random random, float difficulty) {
//        // fetch a location using the difficulty
//        if (!Runestones.DESTINATIONS.containsKey(dimension)) {
//            LogHelper.debug(RunestoneHelper.class, "No locations available for this dimension, giving up");
//            return Optional.empty();
//        }
//
//        List<ResourceLocation> dimensionDestinations = new ArrayList<>(Runestones.DESTINATIONS.get(dimension));
//        int index = Math.min(dimensionDestinations.size() - 1, (int)Math.ceil(dimensionDestinations.size() * difficulty));
//        if (index == 1 && random.nextFloat() < 0.5F) {
//            index = 0;
//        }
//        return Optional.of(dimensionDestinations.get(index));
//    }

//    public static Optional<Discovery> getOrCreateDestination(ResourceLocation dimension, Random random, float difficulty, float decay, @Nullable ResourceLocation id) {
//        KnowledgeData knowledge = Knowledge.getKnowledgeData().orElseThrow();
//
//        if (id != null) {
//            if (id.equals(Runestones.SPAWN)) {
//                // does it exist?
//                Optional<Discovery> optSpawn = knowledge.specials.values().stream().filter(d -> d.getLocation().equals(Runestones.SPAWN)).findFirst();
//                if (optSpawn.isPresent()) {
//                    // generate items for it
//                    Discovery spawn = optSpawn.get();
//                    return Optional.of(spawn);
//                }
//
//                // generate a new set of runes for it, check if they already exist
//                String runes = KnowledgeHelper.tryGenerateUniqueId(knowledge.specials, random, difficulty, 1, 1).orElseThrow();
//
//                Discovery discovery = new Discovery(runes, Runestones.SPAWN);
//                knowledge.specials.register(discovery);
//                return Optional.of(discovery);
//            }
//        } else {
//            Optional<ResourceLocation> optLocation = tryFetchDestinationByDifficulty(dimension, random, difficulty);
//            if (optLocation.isEmpty()) {
//                return Optional.empty();
//            }
//
//            id = optLocation.get();
//        }
//
//        String runes = KnowledgeHelper.tryGenerateUniqueId(knowledge.discoveries, random, difficulty, Knowledge.MIN_LENGTH, Knowledge.MAX_LENGTH).orElseThrow();
//
//        Discovery discovery = new Discovery(runes, id);
//        discovery.setDifficulty(difficulty);
//        discovery.setDimension(dimension);
//
//        LogHelper.debug(RunestoneHelper.class, "Adding discovery to server knowledge.  Runes = " + runes + ", Location = " + id);
//        knowledge.discoveries.register(discovery);
//        return knowledge.discoveries.get(runes);
//    }
}

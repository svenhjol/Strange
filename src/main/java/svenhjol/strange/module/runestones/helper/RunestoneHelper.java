package svenhjol.strange.module.runestones.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Runes;
import svenhjol.strange.module.runes.Tier;
import svenhjol.strange.module.runestones.Runestones;

import java.util.*;
import java.util.stream.Collectors;

public class RunestoneHelper {
    public static List<Item> getItems(ResourceLocation dimension, String runes) {
        // Create RNG for this runephrase. This guarantees we get the same selection when we shuffle the possible items.
        long seed = RuneHelper.seed(runes);
        Random random = new Random(seed);
        List<Item> items;

        if (!Runestones.ITEMS.containsKey(dimension)) {

            // When there are no items for a given dimension then we use a default list.
            items = new ArrayList<>(Runestones.DEFAULT_ITEMS);

        } else {

            Map<Tier, List<Item>> dimensionItems = Runestones.ITEMS.get(dimension);

            // Calculate the "tier" of items that we will show for this location.
            // This is determined by the length of the rune phrase.
            int len = Math.min(Runes.NUM_RUNES, runes.length());
            int val = Math.max(1, Math.min(Tier.size(), Math.round(Tier.size() * (len / (float) Runes.NUM_RUNES))));
            Tier tier = Tier.byLevel(val);

            if (dimensionItems.containsKey(tier) && !dimensionItems.get(tier).isEmpty()) {
                items = new LinkedList<>(dimensionItems.get(tier));
            } else {
                // When items fail to be fetched fallback to the default list.
                items = new ArrayList<>(Runestones.DEFAULT_ITEMS);
            }
        }

        // Shuffle items according to the seed we generated for this rune phrase and return a selection of items.
        // The maximum number of items returned is limited to the MAX_ITEMS constant.
        Collections.shuffle(items, random);
        return items.subList(0, Math.min(items.size(), Runestones.MAX_ITEMS))
            .stream()
            .distinct()
            .collect(Collectors.toList());
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
}

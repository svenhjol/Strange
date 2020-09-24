package svenhjol.strange.scroll.populator;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scroll.ScrollQuest;
import svenhjol.strange.scroll.ScrollDefinition;

import java.util.*;

public class RewardPopulator extends Populator {
    public static final String ITEMS = "items";
    public static final String XP = "xp";
    public static final String COUNT = "count";
    public static final String LEVELS = "levels";

    public RewardPopulator(World world, BlockPos pos, ScrollQuest scrollQuest, ScrollDefinition definition) {
        super(world, pos, scrollQuest, definition);
    }

    @Override
    public void populate() {
        Map<String, Map<String, String>> reward = definition.getReward();
        if (reward.isEmpty())
            return;

        if (reward.containsKey(COUNT))
            throw new RuntimeException("Reward XP count has been deprecated. Fix up scroll definition: " + definition.getTitle());

        if (reward.containsKey(ITEMS)) {
            Map<String, String> defined = reward.get(ITEMS);
            Map<ItemStack, Integer> items = new HashMap<>();

            for (String stackName : defined.keySet()) {
                ItemStack stack = getItemFromKey(stackName);
                if (stack == null)
                    continue;

                // reward scales the number of items according to the rarity of the scroll
                int count = getCountFromValue(defined.get(stackName), true);
                items.put(stack, count);
            }

            // if more than 3 items, shuffle the set and take the top 3
            if (items.size() > 3) {
                List<ItemStack> itemList = new ArrayList<>(items.keySet());
                Collections.shuffle(itemList);
                itemList.subList(0, 3).forEach(stack -> scrollQuest.getReward().addItem(stack, items.get(stack)));
            } else {
                items.forEach(scrollQuest.getReward()::addItem);
            }
        }

        if (reward.containsKey(XP)) {
            Map<String, String> definition = reward.get(XP);
            if (definition.containsKey(LEVELS)) {

                // reward scales the number of levels according to the rarity of the scroll
                int levels = getCountFromValue(definition.get(LEVELS), true);
                scrollQuest.getReward().setXp(levels);
            }
        }
    }
}

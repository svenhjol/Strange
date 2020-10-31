package svenhjol.strange.scrolls.populator;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import svenhjol.strange.scrolls.JsonDefinition;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.*;

public class RewardPopulator extends Populator {
    public static final String ITEMS = "items";
    public static final String LEVELS = "levels";
    public static final String XP = "xp";

    public RewardPopulator(ServerPlayerEntity player, Quest quest, JsonDefinition definition) {
        super(player, quest, definition);
    }

    @Override
    public void populate() {
        Map<String, Map<String, String>> reward = definition.getReward();
        if (reward.isEmpty())
            return;

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
                itemList.subList(0, 3).forEach(stack -> quest.getReward().addItem(stack, items.get(stack)));
            } else {
                items.forEach(quest.getReward()::addItem);
            }
        }

        if (reward.containsKey(XP)) {
            Map<String, String> definition = reward.get(XP);
            if (definition.containsKey(LEVELS)) {

                // reward scales the number of levels according to the rarity of the scroll
                int levels = getCountFromValue(definition.get(LEVELS), true);
                quest.getReward().setLevels(levels);
            }
        }
    }
}

package svenhjol.strange.feature.quests.reward;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.data.LinkedItemList;
import svenhjol.strange.data.LinkedResourceList;
import svenhjol.strange.data.ResourceListManager;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DefaultRewards {
    private final List<RewardItem> items = new ArrayList<>();
    private final List<RewardExperience> experience = new ArrayList<>();

    public DefaultRewards(Quest quest, ResourceManager manager, QuestDefinition definition) {
        var random = quest.random();
        var entries = ResourceListManager.entries(manager, "quests/reward");
        var sampleSize = 4; // Number of items to fetch from each reward entry
        var additionalChance = 0.2d; // Chance of reward size being increased by one
        List<RewardItem> sampleItems = new ArrayList<>();

        // Populate the reward functions.
        List<String> rewardFunctionIds = new ArrayList<>();

        // Default reward functions.
        var defaultIds = LinkedResourceList.load(entries.getOrDefault(Quests.DEFAULT_REWARD_FUNCTIONS, new LinkedList<>()));
        defaultIds.forEach(id -> rewardFunctionIds.add(id.getPath()));

        // Reward functions defined in the definition.
        for (var functionEntry : definition.rewardFunctions()) {
            var functionIds = LinkedResourceList.load(entries.getOrDefault(functionEntry, new LinkedList<>()));
            functionIds.forEach(id -> rewardFunctionIds.add(id.getPath()));
        }

        // Populate the reward items.
        for (var rewardItemEntries : definition.rewards()) {
            var rewardItemEntry = rewardItemEntries.getFirst();
            var rewardItemAmount = rewardItemEntries.getSecond();

            var items = LinkedItemList.load(entries.getOrDefault(rewardItemEntry, new LinkedList<>()));
            if (items.isEmpty()) {
                continue;
            }

            Collections.shuffle(items);

            // Get a selection of items.
            for (int i = 0; i < Math.min(sampleSize, items.size()); i++) {
                var rewardItem = items.get(i);

                var stack = new ItemStack(rewardItem,
                    random.nextIntBetweenInclusive(Math.max(1, rewardItemAmount - 2), rewardItemAmount));

                // Apply reward functions to the item.
                var item = new RewardItem(quest, stack);
                for (var functionId : rewardFunctionIds) {
                    Quests.REWARD_ITEM_FUNCTIONS.byId(functionId).ifPresent(f -> f.apply(item));
                }

                sampleItems.add(item);
            }
        }

        Collections.shuffle(sampleItems);

        // Get a subselection of the sampleItems. The sublist size is the same as villager level with chance for +1.
        var amount = Math.min(definition.level() + (random.nextDouble() < additionalChance ? 1 : 0), Quests.maxQuestRewards);
        this.items.addAll(sampleItems.subList(0, Math.min(amount, sampleItems.size())));

        // Populate XP.
        var xp = new RewardExperience(quest, definition.rewardExperience());
        this.experience.add(xp);
    }

    public List<RewardItem> items() {
        return items;
    }

    public List<RewardExperience> experience() {
        return experience;
    }
}

package svenhjol.strange.feature.quests.reward;

import net.minecraft.Util;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultRewards {
    private final List<RewardItem> rewardItems;
    private final RewardExperience rewardExperience;

    public DefaultRewards(Quest quest, QuestDefinition definition) {
        var random = quest.random();

        var items = definition.rewardItems().stream()
            .flatMap(i -> i.items().stream()).collect(Collectors.toCollection(ArrayList::new));

        // Assign the current quest to each item.
        items.forEach(i -> i.setQuest(quest));

        var functions = definition.rewardItemFunctions().stream()
            .map(RewardItemFunctionDefinition::function).collect(Collectors.toCollection(ArrayList::new));

        if (functions.isEmpty()) {
            // Set up some default reward item functions if none were specified.
            var emptyFunctionDefinition = new RewardItemFunctionDefinition(definition);
            var emptyFunctionParameters = new RewardItemFunctionParameters(emptyFunctionDefinition);

            functions.add(Quests.REWARD_ITEM_FUNCTIONS.byId("enchant_item", emptyFunctionParameters));
            functions.add(Quests.REWARD_ITEM_FUNCTIONS.byId("enchant_book", emptyFunctionParameters));
        }

        Map<String, List<RewardItemFunction>> functionCounts = new HashMap<>();

        for (var func : functions) {
            functionCounts.computeIfAbsent(func.id(), a -> new ArrayList<>()).add(func);
        }

        for (String s : functionCounts.keySet()) {
            var funcs = functionCounts.get(s);
            if (funcs.size() > 1) {
                Util.shuffle(funcs, random);
            }
            var func = funcs.get(0);
            items.forEach(func::apply);
        }

        // We want a subset of the possible items.
        var max = Math.min(definition.level(), Quests.maxQuestRewards - 1);
        Util.shuffle(items, random);
        rewardItems = items.subList(0, Math.min(max, items.size()));

        // Populate experience levels.
        rewardExperience = new RewardExperience(quest, definition.rewardExperience());


//        var rewardItems = definition.rewardItems();
//        rewardItems.forEach(i -> allItems.addAll(i.items()));
//
//
//        var random = quest.random();
//        var entries = ResourceListManager.entries(manager, "quests/reward");
//        var sampleSize = 4; // Number of items to fetch from each reward entry
//        var additionalChance = 0.2d; // Chance of reward size being increased by one
//        List<RewardItem> sampleItems = new ArrayList<>();
//
//        // Populate the reward functions.
//        List<String> rewardFunctionIds = new ArrayList<>();
//
//        // Default reward functions.
//        var defaultIds = LinkedResourceList.load(entries.getOrDefault(Quests.DEFAULT_REWARD_FUNCTIONS, new LinkedList<>()));
//        defaultIds.forEach(id -> rewardFunctionIds.add(id.getPath()));
//
//        // Reward functions defined in the definition.
//        for (var functionEntry : definition.rewardItemFunctions()) {
//            var functionIds = LinkedResourceList.load(entries.getOrDefault(functionEntry, new LinkedList<>()));
//            functionIds.forEach(id -> rewardFunctionIds.add(id.getPath()));
//        }
//
//        // Populate the reward items.
//        for (var rewardItemEntries : definition.rewardItems()) {
//            var rewardItemEntry = rewardItemEntries.getFirst();
//            var rewardItemAmount = rewardItemEntries.getSecond();
//
//            var items = LinkedItemList.load(entries.getOrDefault(rewardItemEntry, new LinkedList<>()));
//            if (items.isEmpty()) {
//                continue;
//            }
//
//            Collections.shuffle(items);
//
//            // Get a selection of items.
//            for (int i = 0; i < Math.min(sampleSize, items.size()); i++) {
//                var rewardItem = items.get(i);
//
//                var stack = new ItemStack(rewardItem,
//                    random.nextIntBetweenInclusive(Math.max(1, rewardItemAmount - 2), rewardItemAmount));
//
//                // Apply reward functions to the item.
//                var item = new RewardItem(quest, stack);
//                for (var functionId : rewardFunctionIds) {
//                    Quests.REWARD_ITEM_FUNCTIONS.byId(functionId).ifPresent(f -> f.apply(item));
//                }
//
//                sampleItems.add(item);
//            }
//        }
//
//        Collections.shuffle(sampleItems);
//
//        // Get a subselection of the sampleItems. The sublist size is the same as villager level with chance for +1.
//        var amount = Math.min(definition.level() + (random.nextDouble() < additionalChance ? 1 : 0), Quests.maxQuestRewards);
//        this.items.addAll(sampleItems.subList(0, Math.min(amount, sampleItems.size())));
//
//        // Populate XP.
//        this.experience = new RewardExperience(quest, definition.rewardExperience());
    }

    public List<RewardItem> items() {
        return rewardItems;
    }

    public RewardExperience experience() {
        return rewardExperience;
    }
}

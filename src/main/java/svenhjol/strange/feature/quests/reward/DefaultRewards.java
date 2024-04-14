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

        // We want a subset of the possible items.
        var max = Math.min(Math.max(2, definition.level()), Quests.maxQuestRewards);
        Util.shuffle(items, random);
        var limitedItems = items.subList(0, Math.min(max, items.size()));

        // Assign the current quest to each item.
        limitedItems.forEach(i -> i.setQuest(quest));

        var functions = definition.rewardItemFunctions().stream()
            .map(RewardItemFunctionDefinition::function).collect(Collectors.toCollection(ArrayList::new));

        // Some functions are always present. Create defaults if not defined.
        var defaultFunctions = List.of("enchant_book", "enchant_item", "make_potion", "make_suspicious_stew", "make_tipped_arrow");
        var emptyFunctionDefinition = new RewardItemFunctionDefinition(definition);
        var emptyFunctionParameters = new RewardItemFunctionParameters(emptyFunctionDefinition);

        for (var defaultFunction : defaultFunctions) {
            if (functions.stream().noneMatch(f -> f.id().equals(defaultFunction))) {
                functions.add(Quests.REWARD_ITEM_FUNCTIONS.byId(defaultFunction, emptyFunctionParameters));
            }
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
            limitedItems.forEach(func::apply);
        }

        var multiplier = definition.rewardMultiplier();

        // Populate experience levels.
        var experience = new RewardExperience(quest, definition.rewardExperience());

        // Apply multiplier to each reward item and experience
        limitedItems.forEach(i -> i.applyMultiplier(multiplier));
        experience.applyMultiplier(multiplier);

        rewardItems = limitedItems;
        rewardExperience = experience;
    }

    public List<RewardItem> items() {
        return rewardItems;
    }

    public RewardExperience experience() {
        return rewardExperience;
    }
}

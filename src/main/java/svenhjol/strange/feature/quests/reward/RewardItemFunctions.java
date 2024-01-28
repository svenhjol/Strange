package svenhjol.strange.feature.quests.reward;

import svenhjol.strange.feature.quests.reward.reward_item_function.EnchantBookByLevel;
import svenhjol.strange.feature.quests.reward.reward_item_function.EnchantItemByLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RewardItemFunctions {
    List<RewardItemFunction> functions = new ArrayList<>();

    public RewardItemFunctions() {
        populate();
    }

    public Optional<RewardItemFunction> byId(String id) {
        return functions.stream().filter(f -> f.id().equals(id)).findFirst();
    }

    /**
     * Add custom functions to this list. Populated when server starts.
     */
    void populate() {
        functions.add(new EnchantBookByLevel());
        functions.add(new EnchantItemByLevel());
    }
}

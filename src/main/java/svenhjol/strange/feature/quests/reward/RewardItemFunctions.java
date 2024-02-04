package svenhjol.strange.feature.quests.reward;

import svenhjol.strange.feature.quests.reward.function.EnchantBook;
import svenhjol.strange.feature.quests.reward.function.EnchantItem;
import svenhjol.strange.feature.quests.reward.function.MakeStructureMap;

import java.util.ArrayList;
import java.util.List;

public class RewardItemFunctions {
    List<RewardItemFunction> functions = new ArrayList<>();

    public RewardItemFunctions() {
        populate();
    }

    public RewardItemFunction byId(String id, RewardItemFunctionParameters parameters) {
        return functions.stream().filter(f -> f.id().equals(id))
            .findFirst().orElseThrow()
            .withParameters(parameters);
    }

    /**
     * Add custom functions to this list. Populated when server starts.
     */
    void populate() {
        functions.add(new EnchantBook());
        functions.add(new EnchantItem());
        functions.add(new MakeStructureMap());
    }
}

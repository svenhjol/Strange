package svenhjol.strange.feature.quests.reward;

import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.reward.function.*;

import java.util.ArrayList;
import java.util.List;

public class RewardItemFunctions {
    List<Class<? extends RewardItemFunction>> functionClasses = new ArrayList<>();

    public RewardItemFunctions() {
        populate();
    }

    public RewardItemFunction byId(String id, RewardItemFunctionParameters parameters) {
        var clazz = functionClasses.stream().filter(f -> TextHelper.snakeToUpperCamel(id).equals(f.getSimpleName()))
            .findFirst().orElseThrow();

        RewardItemFunction inst;

        try {
            inst = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return inst.withParameters(parameters);
    }

    /**
     * Add custom functions to this list. Populated when server starts.
     */
    void populate() {
        functionClasses.add(EnchantBook.class);
        functionClasses.add(EnchantItem.class);
        functionClasses.add(MakeStructureMap.class);
        functionClasses.add(MakeSuspiciousStew.class);
        functionClasses.add(MakeTippedArrow.class);
    }
}

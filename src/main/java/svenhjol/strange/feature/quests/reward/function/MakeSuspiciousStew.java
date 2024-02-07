package svenhjol.strange.feature.quests.reward.function;

import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardItemFunction;
import svenhjol.strange.feature.quests.reward.RewardItemFunctionParameters;

public class MakeSuspiciousStew implements RewardItemFunction {
    public static final String ID = "make_structure_map";

    private Parameters params;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public RewardItemFunction withParameters(RewardItemFunctionParameters params) {
        this.params = new Parameters(params);
        return this;
    }

    @Override
    public void apply(RewardItem reward) {
        var quest = reward.quest;
        var stack = reward.stack;
        var random = quest.random();
    }

    public static class Parameters {

        public Parameters(RewardItemFunctionParameters params) {
        }
    }
}

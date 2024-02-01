package svenhjol.strange.feature.quests.reward;

public interface RewardItemFunction {
    String id();

    RewardItemFunction withParameters(RewardItemFunctionParameters parameters);

    void apply(RewardItem reward);
}

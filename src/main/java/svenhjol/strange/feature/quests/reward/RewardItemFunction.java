package svenhjol.strange.feature.quests.reward;

public interface RewardItemFunction {
    String id();

    void apply(RewardItem reward);
}

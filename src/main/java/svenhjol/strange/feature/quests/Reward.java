package svenhjol.strange.feature.quests;

import net.minecraft.nbt.CompoundTag;

public interface Reward {
    Quest.RewardType type();

    /**
     * Run when the quest is started.
     */
    void start();

    /**
     * Run when the quest is completed.
     */
    void complete();

    void load(CompoundTag tag);

    void save(CompoundTag tag);
}

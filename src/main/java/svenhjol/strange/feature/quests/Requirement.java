package svenhjol.strange.feature.quests;

import net.minecraft.nbt.CompoundTag;

public interface Requirement {
    boolean satisfied();

    int total();

    int remaining();

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

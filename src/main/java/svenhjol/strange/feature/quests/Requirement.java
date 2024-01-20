package svenhjol.strange.feature.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface Requirement {
    boolean satisfied();

    int total();

    int remaining();

    /**
     * Run when the quest is started.
     */
    default void start() {
        // no op
    }

    /**
     * Run when the quest is completed.
     */
    default void complete() {
        // no op
    }

    /**
     * Run when any entity is killed by the quest player.
     */
    default void entityKilled(LivingEntity entity, DamageSource source) {
        // no op
    }

    void load(CompoundTag tag);

    void save(CompoundTag tag);
}

package svenhjol.strange.data;

import net.minecraft.nbt.CompoundTag;

public interface Serializable {
    void loadAdditional(CompoundTag tag);

    CompoundTag save();

    default void saveAdditional(CompoundTag tag) {
        // no op
    }
}

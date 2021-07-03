package svenhjol.strange.module.scrolls.nbt;

import net.minecraft.nbt.CompoundTag;

public interface IQuestSerializable {
    CompoundTag toNbt();

    void fromNbt(CompoundTag nbt);
}

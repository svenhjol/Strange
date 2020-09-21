package svenhjol.strange.scroll;

import net.minecraft.nbt.CompoundTag;

public interface IScrollSerializable {
    CompoundTag toTag();

    void fromTag(CompoundTag tag);
}

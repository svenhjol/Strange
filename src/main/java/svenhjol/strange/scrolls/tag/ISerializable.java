package svenhjol.strange.scrolls.tag;

import net.minecraft.nbt.CompoundTag;

public interface ISerializable {
    CompoundTag toTag();
    void fromTag(CompoundTag tag);
}

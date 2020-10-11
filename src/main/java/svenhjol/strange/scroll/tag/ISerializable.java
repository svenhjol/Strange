package svenhjol.strange.scroll.tag;

import net.minecraft.nbt.CompoundTag;

public interface ISerializable {
    CompoundTag toTag();
    void fromTag(CompoundTag tag);
}

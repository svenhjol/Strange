package svenhjol.strange.scroll.tag;

import net.minecraft.nbt.CompoundTag;

public interface ITag {
    CompoundTag toTag();
    void fromTag(CompoundTag tag);
}

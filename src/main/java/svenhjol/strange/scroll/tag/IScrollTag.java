package svenhjol.strange.scroll.tag;

import net.minecraft.nbt.CompoundTag;

public interface IScrollTag {
    CompoundTag toTag();

    void fromTag(CompoundTag tag);
}

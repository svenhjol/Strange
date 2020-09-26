package svenhjol.strange.scroll;

import net.minecraft.nbt.CompoundTag;

public interface IScrollTag {
    CompoundTag toTag();

    void fromTag(CompoundTag tag);
}

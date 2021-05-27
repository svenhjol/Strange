package svenhjol.strange.scroll.tag;

import net.minecraft.nbt.NbtCompound;

public interface ISerializable {
    NbtCompound toTag();
    void fromTag(NbtCompound tag);
}

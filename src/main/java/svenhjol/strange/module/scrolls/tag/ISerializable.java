package svenhjol.strange.module.scrolls.tag;

import net.minecraft.nbt.NbtCompound;

public interface ISerializable {
    NbtCompound toTag();
    void fromTag(NbtCompound tag);
}

package svenhjol.strange.iface;

import net.minecraft.nbt.CompoundTag;

public interface ISerializable {
    CompoundTag save();

    void load(CompoundTag tag);
}

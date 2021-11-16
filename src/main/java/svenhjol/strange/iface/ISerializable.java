package svenhjol.strange.iface;

import net.minecraft.nbt.CompoundTag;

public interface ISerializable {
    CompoundTag toNbt();

    void fromNbt(CompoundTag tag);
}

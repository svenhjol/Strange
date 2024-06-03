package svenhjol.strange.api.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.api.enums.RunestoneLocationType;

public record RunestoneLocation(RunestoneLocationType type, ResourceLocation id) {
    public static final String TYPE_TAG = "type";
    public static final String ID_TAG = "id";

    public boolean is(RunestoneLocation location) {
        return type.equals(location.type()) && id.equals(location.id());
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putString(TYPE_TAG, type().name());
        tag.putString(ID_TAG, id().toString());
        return tag;
    }

    public static RunestoneLocation load(CompoundTag tag) {
        var type = RunestoneLocationType.valueOf(tag.getString(TYPE_TAG));
        var id = ResourceLocation.parse(tag.getString(ID_TAG));
        return new RunestoneLocation(type, id);
    }
}

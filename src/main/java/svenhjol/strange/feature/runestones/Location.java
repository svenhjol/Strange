package svenhjol.strange.feature.runestones;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class Location {
    static final String TYPE_TAG = "type";
    static final String ID_TAG = "id";
    private LocationType type;
    private ResourceLocation id;

    public Location(LocationType type, ResourceLocation id) {
        this.type = type;
        this.id = id;
    }

    private Location() {}

    public boolean is(Location location) {
        return type.equals(location.type) && id.equals(location.id);
    }

    public LocationType type() {
        return type;
    }

    public ResourceLocation id() {
        return id;
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putString(TYPE_TAG, type.name());
        tag.putString(ID_TAG, id.toString());
        return tag;
    }

    public static Location load(CompoundTag tag) {
        var location = new Location();
        location.type = LocationType.valueOf(tag.getString(TYPE_TAG));
        location.id = new ResourceLocation(tag.getString(ID_TAG));
        return location;
    }
}

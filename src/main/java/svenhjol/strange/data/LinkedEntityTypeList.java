package svenhjol.strange.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;

import java.util.LinkedList;
import java.util.List;

public class LinkedEntityTypeList extends LinkedList<EntityType<?>> implements WeightedList<EntityType<?>>, Serializable {
    private static final String VALUES_TAG = "Values";

    public static LinkedEntityTypeList load(List<ResourceLocation> list) {
        var collection = new LinkedEntityTypeList();
        list.forEach(r -> BuiltInRegistries.ENTITY_TYPE.getOptional(r).ifPresentOrElse(collection::add,
            () -> log().warn(LinkedEntityTypeList.class, "Invalid entity: " + r)));
        return collection;
    }

    public static LinkedEntityTypeList load(CompoundTag tag) {
        var collection = new LinkedEntityTypeList();
        collection.loadAdditional(tag);
        return collection;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        var valuesList = tag.getList(VALUES_TAG, 8);

        for (int i = 0; i < valuesList.size(); i++) {
            var value = valuesList.getString(i);
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(value));
            add(entityType);
        }
    }

    public CompoundTag save() {
        var tag = new CompoundTag();

        ListTag valuesList = new ListTag();
        var values = values();

        for (int i = 0; i < values.size(); i++) {
            valuesList.add(i, StringTag.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(values.get(i)).toString()));
        }

        tag.put(VALUES_TAG, valuesList);
        return tag;
    }

    @Override
    public LinkedList<EntityType<?>> values() {
        return new LinkedList<>(this);
    }

    private static ILog log() {
        return Mods.common(Strange.ID).log();
    }
}

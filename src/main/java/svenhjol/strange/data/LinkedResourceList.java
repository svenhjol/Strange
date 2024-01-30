package svenhjol.strange.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

public class LinkedResourceList extends LinkedList<ResourceLocation> implements WeightedList<ResourceLocation>, Serializable {
    private static final String VALUES_TAG = "values";

    public static LinkedResourceList load(List<ResourceLocation> list) {
        var collection = new LinkedResourceList();
        collection.addAll(list);
        return collection;
    }

    public static LinkedResourceList load(CompoundTag tag) {
        var collection = new LinkedResourceList();
        collection.loadAdditional(tag);
        return collection;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        var valuesList = tag.getList(VALUES_TAG, 8);

        for (int i = 0; i < valuesList.size(); i++) {
            var value = valuesList.getString(i);
            add(new ResourceLocation(value));
        }
    }

    public CompoundTag save() {
        var tag = new CompoundTag();

        ListTag valuesList = new ListTag();
        var values = values();

        for (int i = 0; i < values.size(); i++) {
            valuesList.add(i, StringTag.valueOf(values.get(i).toString()));
        }

        tag.put(VALUES_TAG, valuesList);
        return tag;
    }

    @Override
    public LinkedList<ResourceLocation> values() {
        return new LinkedList<>(this);
    }
}

package svenhjol.strange.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;

import java.util.LinkedList;
import java.util.List;

public class LinkedItemList extends LinkedList<Item> implements WeightedList<Item>, Serializable {
    private static final String VALUES_TAG = "values";

    public static LinkedItemList load(List<ResourceLocation> list) {
        var collection = new LinkedItemList();
        list.forEach(r -> BuiltInRegistries.ITEM.getOptional(r).ifPresentOrElse(collection::add,
            () -> log().warn(LinkedItemList.class, "Invalid item: " + r)));
        return collection;
    }

    public static LinkedItemList load(CompoundTag tag) {
        var collection = new LinkedItemList();
        collection.loadAdditional(tag);
        return collection;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        var valuesList = tag.getList(VALUES_TAG, 8);

        for (int i = 0; i < valuesList.size(); i++) {
            var value = valuesList.getString(i);
            var opt = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(value));
            opt.ifPresent(this::add);
        }
    }

    @Override
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
    public LinkedList<Item> values() {
        return new LinkedList<>(this);
    }

    private static ILog log() {
        return Mods.common(Strange.ID).log();
    }
}

package svenhjol.strange.feature.travel_journal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Learned {
    public static final String DESTINATIONS_TAG = "destinations";
    private List<ResourceLocation> destinations = new ArrayList<>();

    public void learn(ResourceLocation id) {
        if (!learned(id)) {
            destinations.add(id);
        }
    }

    public boolean learned(ResourceLocation id) {
        return destinations.contains(id);
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        var list = new ListTag();
        destinations.forEach(destination -> list.add(StringTag.valueOf(destination.toString())));
        tag.put(DESTINATIONS_TAG, list);
        return tag;
    }

    public static Learned load(CompoundTag tag) {
        var learned = new Learned();
        var list = tag.getList(DESTINATIONS_TAG, 8);
        learned.destinations = list.stream()
            .map(Tag::getAsString)
            .map(ResourceLocation::new)
            .collect(Collectors.toCollection(ArrayList::new));

        return learned;
    }
}

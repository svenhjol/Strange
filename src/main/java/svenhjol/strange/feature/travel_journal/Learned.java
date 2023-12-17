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
    public static final String LOCATIONS_TAG = "locations";
    private List<ResourceLocation> locations = new ArrayList<>();

    public void learn(ResourceLocation id) {
        if (!hasLearned(id)) {
            locations.add(id);
        }
    }

    public boolean hasLearned(ResourceLocation id) {
        return locations.contains(id);
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        var list = new ListTag();
        locations.forEach(location -> list.add(StringTag.valueOf(location.toString())));
        tag.put(LOCATIONS_TAG, list);
        return tag;
    }

    public static Learned load(CompoundTag tag) {
        var learned = new Learned();
        var list = tag.getList(LOCATIONS_TAG, 8);
        learned.locations = list.stream()
            .map(Tag::getAsString)
            .map(ResourceLocation::new)
            .collect(Collectors.toCollection(ArrayList::new));

        return learned;
    }
}

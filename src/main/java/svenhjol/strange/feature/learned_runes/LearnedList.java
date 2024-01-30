package svenhjol.strange.feature.learned_runes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import svenhjol.strange.feature.runestones.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LearnedList {
    public static final String LOCATIONS_TAG = "locations";
    private List<Location> locations = new ArrayList<>();

    public void learn(Location location) {
        if (!hasLearned(location)) {
            locations.add(location);
        }
    }

    public List<Location> all() {
        return locations;
    }

    public boolean hasLearned(Location location) {
        return locations.stream().anyMatch(l -> l.is(location));
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        var list = new ListTag();
        locations.forEach(location -> list.add(location.save()));
        tag.put(LOCATIONS_TAG, list);
        return tag;
    }

    public static LearnedList load(CompoundTag tag) {
        var learned = new LearnedList();
        var list = tag.getList(LOCATIONS_TAG, 10);
        learned.locations = list.stream()
            .map(t -> Location.load((CompoundTag)t))
            .collect(Collectors.toCollection(ArrayList::new));

        return learned;
    }
}

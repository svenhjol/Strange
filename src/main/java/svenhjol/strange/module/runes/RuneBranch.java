package svenhjol.strange.module.runes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class RuneBranch<R, V> {
    protected final Map<String, V> data = new HashMap<>();

    public Map<String, V> all() {
        return data;
    }

    public List<String> keys() {
        return new ArrayList<>(data.keySet());
    }

    public List<V> values() {
        return new ArrayList<>(data.values());
    }

    public int size() {
        return data.size();
    }

    public boolean contains(String runes) {
        return data.containsKey(runes);
    }

    public boolean contains(V value) {
        return data.containsValue(value);
    }

    @Nullable
    public V get(String runes) {
        return data.get(runes);
    }

    @Nullable
    public String get(V value) {
        Optional<String> key = all().entrySet().stream()
            .filter(entry -> entry.getValue().equals(value))
            .map(Map.Entry::getKey)
            .findFirst();

        return key.orElse(null);
    }

    public void add(String runes, V value) {
        data.put(runes, value);
    }

    public V remove(String runes) {
        return data.remove(runes);
    }

    public void clear() {
        data.clear();
    }

    public void save(CompoundTag mainTag) {
        CompoundTag tag = new CompoundTag();
        data.forEach((runes, value) -> tag.put(runes, getValueTag(value)));
        mainTag.put(getBranchName(), tag);
    }

    public abstract V register(R type);

    public abstract Tag getValueTag(V value);

    public abstract char getStartRune();

    @Nullable
    public abstract String getValueName(String runes);

    public abstract String getBranchName();
}

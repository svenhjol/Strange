package svenhjol.strange.module.knowledge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public abstract class KnowledgeBranch<R, V> {
    protected Map<String, V> data = new HashMap<>();

    public KnowledgeBranch() {
        if (!KnowledgeData.getBranchInstances().contains(this)) {
            KnowledgeData.getBranchInstances().add(this);
        }
        KnowledgeData.getMappedByStartRune().put(getStartRune(), this);
    }

    public Map<String, V> all() {
        return data;
    }

    public List<String> keys() {
        return new ArrayList<>(data.keySet());
    }

    public int size() {
        return data.size();
    }

    public List<V> values() {
        return new ArrayList<>(data.values());
    }

    public void add(String runes, V value) {
        data.put(runes, value);
        setChanged();
    }

    public Optional<V> get(String runes) {
        return Optional.ofNullable(data.get(runes));
    }

    public Optional<String> get(V value) {
        return data.entrySet().stream().filter(e -> e.getValue().equals(value)).map(Map.Entry::getKey).findFirst();
    }

    public boolean has(String runes) {
        return data.containsKey(runes);
    }

    public Optional<V> first() {
        return values().stream().findFirst();
    }

    public V remove(String runes) {
        V removed = data.remove(runes);
        setChanged();
        return removed;
    }

    public void clear() {
        data = new HashMap<>();
    }

    public void save(CompoundTag masterTag) {
        CompoundTag tag = new CompoundTag();
        data.forEach((runes, value) -> tag.put(runes, tagify(value)));
        masterTag.put(getBranchName(), tag);
        setChanged();
    }

    public void setChanged() {
        Knowledge.getKnowledgeData().ifPresent(KnowledgeData::setDirty);
    }

    public abstract void register(R type);

    protected abstract Tag tagify(V value);

    public abstract String getBranchName();

    public abstract Optional<String> getPrettyName(String runes);

    public abstract char getStartRune();

    /**
     * If true, this branch contains entries that can be added to the player's journal.
     */
    public abstract boolean isLearnable();

    public static Optional<KnowledgeBranch<?, ?>> getByName(String name) {
        return getBranches().stream().filter(b -> b.getBranchName().equals(name)).findFirst();
    }

    public static Optional<KnowledgeBranch<?, ?>> getByStartRune(char start) {
        return Optional.ofNullable(KnowledgeData.getMappedByStartRune().get(start));
    }

    public static List<KnowledgeBranch<?, ?>> getBranches() {
        return KnowledgeData.getBranchInstances();
    }
}

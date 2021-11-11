package svenhjol.strange.module.knowledge;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
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
        return data.remove(runes);
    }

    public void clear() {
        data = new HashMap<>();
    }

    public void save(CompoundTag masterTag) {
        CompoundTag tag = new CompoundTag();
        data.forEach((runes, value) -> tag.put(runes, tagify(value)));
        masterTag.put(getBranchName(), tag);
    }

    public abstract void register(R type);

    protected abstract Tag tagify(V value);

    public abstract String getBranchName();

    public abstract Optional<String> getPrettyName(String runes);

    public abstract char getStartRune();

    public boolean travel(String runes, ItemStack sacrifice, LivingEntity entity, @Nullable BlockPos origin) {
        return false;
    }

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

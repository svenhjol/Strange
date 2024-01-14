package svenhjol.strange.feature.quests;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Quest<T> {
    static final String TYPE_TAG = "type";
    static final String VILLAGER_PROFESSION_TAG = "villager_profession";
    static final String VILLAGER_LEVEL_TAG = "villager_level";

    protected @Nullable ServerPlayer player;

    protected QuestType type;

    protected VillagerProfession villagerProfession;
    protected int villagerLevel;

    public QuestType type() {
        return type;
    }

    protected abstract Registry<T> registry();

    protected abstract ResourceKey<Registry<T>> resourceKey();

    public abstract List<? extends Requirement> requirements();

    public abstract List<? extends Reward> rewards();

    public boolean satisfied() {
        return requirements().stream().allMatch(Requirement::satisfied);
    }

    public TagKey<T> tag(ResourceLocation id) {
        return TagKey.create(resourceKey(), id);
    }

    public static <Q extends Quest<?>> Q create(IQuestDefinition definition) {
        var type = definition.type();

        var quest = type.<Q>makeQuest();
        quest.make(definition);
        return quest;
    }

    public static <Q extends Quest<?>> Q load(CompoundTag tag) {
        var type = QuestType.valueOf(tag.getString(TYPE_TAG));
        var villagerProfession = BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(tag.getString(VILLAGER_PROFESSION_TAG)));
        var villagerLevel = tag.getInt(VILLAGER_LEVEL_TAG);

        var quest = type.<Q>makeQuest();
        quest.type = type;
        quest.villagerProfession = villagerProfession;
        quest.villagerLevel = villagerLevel;

        quest.loadAdditional(tag);
        return quest;
    }

    public void save(CompoundTag tag) {
        tag.putString(TYPE_TAG, type.name());
        tag.putString(VILLAGER_PROFESSION_TAG, BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.villagerProfession).toString());
        tag.putInt(VILLAGER_LEVEL_TAG, villagerLevel);

        saveAdditional(tag);
    }

    public void tick(ServerPlayer player) {
        this.player = player;
    }

    protected abstract void make(IQuestDefinition definition);

    public void loadAdditional(CompoundTag tag) {
    }

    public void saveAdditional(CompoundTag tag) {
    }

    public VillagerProfession villagerProfession() {
        return villagerProfession;
    }

    public int villagerLevel() {
        return villagerLevel;
    }

    public interface Requirement {
        boolean satisfied();

        int total();

        int remaining();

        void complete();

        void load(CompoundTag tag);

        void save(CompoundTag tag);
    }

    public interface Reward {
        RewardType type();

        void load(CompoundTag tag);

        void save(CompoundTag tag);
    }

    public enum RewardType {
        ITEM,
        EXPERIENCE_LEVEL
    }
}

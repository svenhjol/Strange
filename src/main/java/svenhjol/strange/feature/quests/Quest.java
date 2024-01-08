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
    static final String PROFESSION_TAG = "profession";

    protected @Nullable ServerPlayer player;

    protected QuestType type;

    protected VillagerProfession profession;

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

        var quest = type.<Q>instance();
        quest.make(definition);
        return quest;
    }

    public static <Q extends Quest<?>> Q load(CompoundTag tag) {
        var type = QuestType.valueOf(tag.getString(TYPE_TAG));
        var profession = BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(tag.getString(PROFESSION_TAG)));

        var quest = type.<Q>instance();
        quest.type = type;
        quest.profession = profession;

        quest.loadAdditional(tag);
        return quest;
    }

    public void save(CompoundTag tag) {
        tag.putString(TYPE_TAG, type.name());
        tag.putString(PROFESSION_TAG, BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.profession).toString());

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

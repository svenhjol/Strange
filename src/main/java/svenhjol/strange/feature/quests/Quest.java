package svenhjol.strange.feature.quests;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public abstract class Quest<T> {
    static final String ID_TAG = "id";
    static final String TYPE_TAG = "type";
    static final String STATUS_TAG = "status";
    static final String VILLAGER_PROFESSION_TAG = "villager_profession";
    static final String VILLAGER_LEVEL_TAG = "villager_level";
    static final String VILLAGER_UUID = "villager_uuid";

    protected @Nullable ServerPlayer player;

    protected String id;

    protected QuestType type;
    protected Status status;

    protected VillagerProfession villagerProfession;
    protected int villagerLevel;
    protected UUID villagerUuid;

    public String id() {
        return id;
    }

    public QuestType type() {
        return type;
    }

    public Status status() {
        return status;
    }

    protected abstract Registry<T> registry();

    protected abstract ResourceKey<Registry<T>> resourceKey();

    public abstract List<? extends Requirement> requirements();

    public abstract List<? extends Reward> rewards();

    public boolean satisfied() {
        return requirements().stream().allMatch(Requirement::satisfied);
    }

    public boolean finished() {
        return status.equals(Status.CANCELLED) || status.equals(Status.COMPLETED);
    }

    public boolean inProgress() {
        return status.equals(Status.STARTED);
    }

    public boolean notStarted() {
        return status.equals(Status.NOT_STARTED);
    }

    public TagKey<T> tag(ResourceLocation id) {
        return TagKey.create(resourceKey(), id);
    }

    public static <Q extends Quest<?>> Q create(IQuestDefinition definition, UUID villagerUuid) {
        var type = definition.type();

        var quest = type.<Q>makeQuest();
        quest.make(definition, villagerUuid);
        return quest;
    }

    public static <Q extends Quest<?>> Q load(CompoundTag tag) {
        var id = tag.getString(ID_TAG);
        var type = QuestType.valueOf(tag.getString(TYPE_TAG));
        var status = Status.valueOf(tag.getString(STATUS_TAG));
        var villagerProfession = BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(tag.getString(VILLAGER_PROFESSION_TAG)));
        var villagerLevel = tag.getInt(VILLAGER_LEVEL_TAG);
        var villagerUuid = tag.getUUID(VILLAGER_UUID);

        var quest = type.<Q>makeQuest();
        quest.id = id;
        quest.type = type;
        quest.status = status;
        quest.villagerProfession = villagerProfession;
        quest.villagerLevel = villagerLevel;
        quest.villagerUuid = villagerUuid;

        quest.loadAdditional(tag);
        return quest;
    }

    public void save(CompoundTag tag) {
        tag.putString(ID_TAG, id);
        tag.putString(TYPE_TAG, type.name());
        tag.putString(STATUS_TAG, status().name());
        tag.putString(VILLAGER_PROFESSION_TAG, BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.villagerProfession).toString());
        tag.putInt(VILLAGER_LEVEL_TAG, villagerLevel);
        tag.putUUID(VILLAGER_UUID, villagerUuid);

        saveAdditional(tag);
    }

    public void tick(ServerPlayer player) {
        this.player = player;
    }

    public void start() {
        if (status == Status.NOT_STARTED) {
            status = Status.STARTED;
        }
    }

    public void cancel() {
        status = Status.CANCELLED;
    }

    public void complete() {
        if (!satisfied()) return;

        requirements().forEach(Requirement::complete);
        rewards().forEach(Reward::complete);

        status = Status.COMPLETED;
    }

    protected abstract void make(IQuestDefinition definition, UUID villagerUuid);

    protected String makeId() {
        return RandomStringUtils.randomAlphanumeric(8).toLowerCase(Locale.ROOT);
    }

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

    public UUID villagerUuid() {
        return villagerUuid;
    }

    public interface Requirement {
        boolean satisfied();

        int total();

        int remaining();

        /**
         * Run when the quest is started.
         */
        void start();

        /**
         * Run when the quest is completed.
         */
        void complete();

        void load(CompoundTag tag);

        void save(CompoundTag tag);
    }

    public interface Reward {
        RewardType type();

        /**
         * Run when the quest is started.
         */
        void start();

        /**
         * Run when the quest is completed.
         */
        void complete();

        void load(CompoundTag tag);

        void save(CompoundTag tag);
    }

    public enum Status {
        NOT_STARTED,
        STARTED,
        COMPLETED,
        CANCELLED
    }

    public enum RewardType {
        ITEM,
        EXPERIENCE_LEVEL
    }
}

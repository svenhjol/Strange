package svenhjol.strange.feature.quests;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.feature.quests.reward.DefaultRewards;
import svenhjol.strange.feature.quests.reward.RewardExperience;
import svenhjol.strange.feature.quests.reward.RewardItem;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings({"unused", "CollectionAddAllCanBeReplacedWithConstructor"})
public abstract class Quest {
    static final String ID_TAG = "id";
    static final String TYPE_TAG = "type";
    static final String STATUS_TAG = "status";
    static final String EPIC_TAG = "epic";
    static final String LOYALTY_TAG = "loyalty";
    static final String DEFINITION_ID_TAG = "definition_id";
    static final String VILLAGER_PROFESSIONS_TAG = "villager_professions";
    static final String VILLAGER_LEVEL_TAG = "villager_level";
    static final String VILLAGER_UUID_TAG = "villager_uuid";
    static final String REWARD_ITEMS_TAG = "reward_items";
    static final String REWARD_XP_TAG = "reward_xp";
    static final String RANDOM_SEED_TAG = "random_seed";

    protected @Nullable Player player;

    protected String id;

    protected QuestType type;
    protected Status status;
    protected UUID villagerUuid;
    protected int villagerLevel;
    protected int loyalty;
    protected List<VillagerProfession> villagerProfessions = new ArrayList<>();
    protected List<RewardItem> rewardItems = new ArrayList<>();
    protected RewardExperience rewardExperience;
    protected long randomSeed;
    protected boolean epic;
    protected String definitionId;
    protected boolean dirty; // transient
    protected RandomSource random; // transient

    public Quest quest() {
        return this;
    }

    public String id() {
        return id;
    }

    public QuestType type() {
        return type;
    }

    public Status status() {
        return status;
    }
    public int loyalty() {
        return loyalty;
    }
    public String definitionId() {
        return definitionId;
    }

    public boolean isEpic() {
        return epic;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean flag) {
        this.dirty = flag;
    }

    public RandomSource random() {
        if (random == null) {
            random = RandomSource.create(randomSeed);
        }
        return random;
    }

    public abstract List<? extends Requirement> requirements();

    public List<? extends Reward> rewards() {
        List<Reward> rewards = new ArrayList<>();

        rewards.addAll(rewardItems());
        rewards.add(rewardExperience());

        return rewards;
    }

    public List<? extends RewardItem> rewardItems() {
        return rewardItems;
    }

    public RewardExperience rewardExperience() {
        return rewardExperience;
    }

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

    public @Nullable Player player() {
        return player;
    }

    public static Quest create(QuestDefinition definition, ServerPlayer player, UUID villagerUuid) {
        var type = definition.type();
        var quest = type.makeQuest();
        quest.make(definition, player, villagerUuid);
        return quest;
    }

    public static Quest load(CompoundTag tag) {
        var id = tag.getString(ID_TAG);
        var type = QuestType.valueOf(tag.getString(TYPE_TAG));
        var status = Status.valueOf(tag.getString(STATUS_TAG));
        var definitionId = tag.getString(DEFINITION_ID_TAG);
        var epic = tag.getBoolean(EPIC_TAG);
        var loyalty = tag.getInt(LOYALTY_TAG);
        var villagerLevel = tag.getInt(VILLAGER_LEVEL_TAG);
        var villagerUuid = tag.getUUID(VILLAGER_UUID_TAG);
        var randomSeed = tag.getLong(RANDOM_SEED_TAG);

        var quest = type.makeQuest();
        quest.id = id;
        quest.type = type;
        quest.status = status;
        quest.epic = epic;
        quest.loyalty = loyalty;
        quest.definitionId = definitionId;
        quest.villagerLevel = villagerLevel;
        quest.villagerUuid = villagerUuid;
        quest.randomSeed = randomSeed;

        // Deserialize villager professions
        quest.villagerProfessions.clear();
        var list = tag.getList(VILLAGER_PROFESSIONS_TAG, 8);
        for (var t : list) {
            var res = ResourceLocation.tryParse(t.getAsString());
            quest.villagerProfessions.add(BuiltInRegistries.VILLAGER_PROFESSION.get(res));
        }

        quest.rewardItems.clear();
        list = tag.getList(REWARD_ITEMS_TAG, 10);
        for (var t : list) {
            var item = new RewardItem();
            item.load((CompoundTag)t);
            item.setQuest(quest);
            quest.rewardItems.add(item);
        }

        var xp = new RewardExperience(quest);
        xp.load(tag.getCompound(REWARD_XP_TAG));
        quest.rewardExperience = xp;

        quest.loadAdditional(tag);
        return quest;
    }

    public void save(CompoundTag tag) {
        tag.putString(ID_TAG, id);
        tag.putString(TYPE_TAG, type.name());
        tag.putString(STATUS_TAG, status().name());
        tag.putString(DEFINITION_ID_TAG, definitionId);
        tag.putBoolean(EPIC_TAG, epic);
        tag.putInt(LOYALTY_TAG, loyalty);
        tag.putInt(VILLAGER_LEVEL_TAG, villagerLevel);
        tag.putUUID(VILLAGER_UUID_TAG, villagerUuid);
        tag.putLong(RANDOM_SEED_TAG, randomSeed);

        var list = new ListTag();
        for (var profession : villagerProfessions) {
            list.add(StringTag.valueOf(BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).toString()));
        }
        tag.put(VILLAGER_PROFESSIONS_TAG, list);

        list = new ListTag();
        for (var item : rewardItems) {
            var t = new CompoundTag();
            item.save(t);
            list.add(t);
        }
        tag.put(REWARD_ITEMS_TAG, list);

        var t = new CompoundTag();
        rewardExperience.save(t);
        tag.put(REWARD_XP_TAG, t);

        saveAdditional(tag);
    }

    protected void make(QuestDefinition definition, ServerPlayer player, UUID villagerUuid) {
        this.id = makeId();
        this.definitionId = definition.id();
        this.type = definition.type();
        this.status = Status.NOT_STARTED;
        this.epic = definition.epic();
        this.loyalty = definition.loyalty();
        this.villagerProfessions = definition.professions();
        this.villagerLevel = definition.level();
        this.villagerUuid = villagerUuid;
        this.randomSeed = definition.seed();
        this.random = definition.random();
        this.player = player;

        makeRequirements(definition);
        makeRewards(definition);
    }

    protected abstract void makeRequirements(QuestDefinition definition);

    protected void makeRewards(QuestDefinition definition) {
        var rewards = new DefaultRewards(this, definition);

        this.rewardItems = rewards.items();
        this.rewardExperience = rewards.experience();
    }

    public void tick(Player player) {
        this.player = player;
    }

    public void start(Player player) {
        this.player = player;

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

    public void entityKilled(LivingEntity entity, DamageSource source) {
        // no op
    }

    public void entityLeave(Entity entity) {
        // no op
    }

    public void playerPickup(ItemStack stack) {
        // no op
    }

    public Optional<ItemStack> addToLootTable(ResourceLocation lootTableId, RandomSource random) {
        return Optional.empty();
    }

    protected String makeId() {
        return RandomStringUtils.randomAlphanumeric(8).toLowerCase(Locale.ROOT);
    }

    public void loadAdditional(CompoundTag tag) {
    }

    public void saveAdditional(CompoundTag tag) {
    }

    public List<VillagerProfession> villagerProfessions() {
        return villagerProfessions;
    }

    public int villagerLevel() {
        return villagerLevel;
    }

    public UUID villagerUuid() {
        return villagerUuid;
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

package svenhjol.strange.feature.quests;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.charmony.helper.TagHelper;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardXp;

import javax.annotation.Nullable;
import java.util.*;

public abstract class Quest {
    static final int MAX_REWARD_ITEMS = 2; // TODO: hardcoded number of item rewards
    static final String ID_TAG = "id";
    static final String TYPE_TAG = "type";
    static final String STATUS_TAG = "status";
    static final String EPIC_TAG = "epic";
    static final String VILLAGER_PROFESSION_TAG = "villager_profession";
    static final String VILLAGER_LEVEL_TAG = "villager_level";
    static final String VILLAGER_UUID = "villager_uuid";
    static final String REWARD_ITEMS_TAG = "reward_items";
    static final String REWARD_XP_TAG = "reward_xp";

    protected @Nullable Player player;

    protected String id;

    protected QuestType type;
    protected Status status;
    protected VillagerProfession villagerProfession;
    protected int villagerLevel;
    protected UUID villagerUuid;
    protected boolean epic;
    protected List<RewardItem> rewardItems = new ArrayList<>();
    protected List<RewardXp> rewardXp = new ArrayList<>();

    public String id() {
        return id;
    }

    public QuestType type() {
        return type;
    }

    public Status status() {
        return status;
    }

    public boolean isEpic() {
        return epic;
    }

    public abstract List<? extends Requirement> requirements();

    public List<? extends Reward> rewards() {
        List<Reward> rewards = new ArrayList<>();
        rewards.addAll(rewardItems());
        rewards.addAll(rewardXp());
        return rewards;
    }

    public List<? extends RewardItem> rewardItems() {
        return rewardItems;
    }

    public List<? extends RewardXp> rewardXp() {
        return rewardXp;
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

    public Registry<Item> itemRegistry() {
        return BuiltInRegistries.ITEM;
    }

    public TagKey<Item> itemTag(ResourceLocation id) {
        return TagKey.create(Registries.ITEM, id);
    }


    public Registry<EntityType<?>> entityRegistry() {
        return BuiltInRegistries.ENTITY_TYPE;
    }

    public TagKey<EntityType<?>> entityTag(ResourceLocation id) {
        return TagKey.create(Registries.ENTITY_TYPE, id);
    }


    public static <Q extends Quest> Q create(ResourceManager manager, QuestDefinition definition, UUID villagerUuid) {
        var type = definition.type();

        var quest = type.<Q>makeQuest();
        quest.make(manager, definition, villagerUuid);
        return quest;
    }

    public static <Q extends Quest> Q load(CompoundTag tag) {
        var id = tag.getString(ID_TAG);
        var type = QuestType.valueOf(tag.getString(TYPE_TAG));
        var status = Status.valueOf(tag.getString(STATUS_TAG));
        var epic = tag.getBoolean(EPIC_TAG);
        var villagerProfession = BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(tag.getString(VILLAGER_PROFESSION_TAG)));
        var villagerLevel = tag.getInt(VILLAGER_LEVEL_TAG);
        var villagerUuid = tag.getUUID(VILLAGER_UUID);

        var quest = type.<Q>makeQuest();
        quest.id = id;
        quest.type = type;
        quest.status = status;
        quest.epic = epic;
        quest.villagerProfession = villagerProfession;
        quest.villagerLevel = villagerLevel;
        quest.villagerUuid = villagerUuid;

        quest.rewardItems.clear();
        var list = tag.getList(REWARD_ITEMS_TAG, 10);
        for (Tag t : list) {
            var item = new RewardItem(quest);
            item.load((CompoundTag)t);
            quest.rewardItems.add(item);
        }

        quest.rewardXp.clear();
        list = tag.getList(REWARD_XP_TAG, 10);
        for (Tag t : list) {
            var xp = new RewardXp(quest);
            xp.load((CompoundTag)t);
            quest.rewardXp.add(xp);
        }

        quest.loadAdditional(tag);
        return quest;
    }

    public void save(CompoundTag tag) {
        tag.putString(ID_TAG, id);
        tag.putString(TYPE_TAG, type.name());
        tag.putString(STATUS_TAG, status().name());
        tag.putBoolean(EPIC_TAG, epic);
        tag.putString(VILLAGER_PROFESSION_TAG, BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.villagerProfession).toString());
        tag.putInt(VILLAGER_LEVEL_TAG, villagerLevel);
        tag.putUUID(VILLAGER_UUID, villagerUuid);

        var list = new ListTag();
        for (RewardItem item : rewardItems) {
            var t = new CompoundTag();
            item.save(t);
            list.add(t);
        }
        tag.put(REWARD_ITEMS_TAG, list);

        list = new ListTag();
        for (RewardXp xp : rewardXp) {
            var t = new CompoundTag();
            xp.save(t);
            list.add(t);
        }
        tag.put(REWARD_XP_TAG, list);

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

    public void entityKilled(LivingEntity entity, DamageSource source) {
        // no op
    }

    public Optional<ItemStack> addToLootTable(ResourceLocation lootTableId, RandomSource random) {
        return Optional.empty();
    }

    protected void make(ResourceManager manager, QuestDefinition definition, UUID villagerUuid) {
        this.id = makeId();
        this.type = definition.type();
        this.status = Status.NOT_STARTED;
        this.epic = definition.isEpic();
        this.villagerProfession = definition.profession();
        this.villagerLevel = definition.level();
        this.villagerUuid = villagerUuid;

        var random = RandomSource.create();
        makeRequirements(manager, definition, random);
        makeRewards(manager, definition, random);
    }

    protected abstract void makeRequirements(ResourceManager manager, QuestDefinition definition, RandomSource random);

    protected void makeRewards(ResourceManager manager, QuestDefinition definition, RandomSource random) {
        var reward = definition.randomReward(random);
        var rewardItem = reward.getFirst();
        var rewardSize = reward.getSecond();
        List<ResourceLocation> values = new ArrayList<>();

        for (var item : TagHelper.getValues(itemRegistry(), itemTag(rewardItem))) {
            values.add(itemRegistry().getKey(item));
        }

        Collections.shuffle(values);

        var selection = Math.min(values.size(), MAX_REWARD_ITEMS);
        for (int i = 0; i < selection; i++) {
            var itemId = values.get(i);
            var stack = new ItemStack(BuiltInRegistries.ITEM.get(itemId),
                random.nextIntBetweenInclusive(Math.max(1, rewardSize - 2), rewardSize));

            var allowTreasure = isEpic() || stack.is(Items.BOOK);
            var enchantChance = 0.2d * villagerLevel();

            if (isEpic() || random.nextDouble() < enchantChance) {
                EnchantmentHelper.enchantItem(random, stack, villagerLevel(), allowTreasure);
            }

            rewardItems.add(new RewardItem(this, stack));
        }

        var xp = new RewardXp(this, definition.experience());
        rewardXp.add(xp);
    }

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

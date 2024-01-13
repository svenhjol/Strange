package svenhjol.strange.feature.quests.quest;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import svenhjol.charmony.helper.TagHelper;
import svenhjol.strange.feature.quests.IQuestDefinition;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GatherQuest extends Quest<Item> {
    static final int MAX_SELECTION = 3;
    static final int MAX_REWARD_ITEMS = 2;
    static final String REQUIRED_ITEMS_TAG = "required";
    static final String REWARD_ITEMS_TAG = "reward_items";
    static final String REWARD_XP_TAG = "reward_xp";

    final List<GatherItem> items = new ArrayList<>();
    final List<RewardItem> rewardItems = new ArrayList<>();
    final List<RewardXp> rewardXp = new ArrayList<>();

    @Override
    protected Registry<Item> registry() {
        return BuiltInRegistries.ITEM;
    }

    @Override
    protected ResourceKey<Registry<Item>> resourceKey() {
        return Registries.ITEM;
    }

    @Override
    public List<? extends Requirement> requirements() {
        return items;
    }

    @Override
    public List<? extends Reward> rewards() {
        List<Reward> rewards = new ArrayList<>();
        rewards.addAll(rewardItems);
        rewards.addAll(rewardXp);
        return rewards;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        items.clear();
        var list = tag.getList(REQUIRED_ITEMS_TAG, 10);
        for (Tag t : list) {
            var item = new GatherItem();
            item.load((CompoundTag)t);
            items.add(item);
        }

        rewardItems.clear();
        list = tag.getList(REWARD_ITEMS_TAG, 10);
        for (Tag t : list) {
            var item = new RewardItem();
            item.load((CompoundTag)t);
            rewardItems.add(item);
        }

        rewardXp.clear();
        list = tag.getList(REWARD_XP_TAG, 10);
        for (Tag t : list) {
            var xp = new RewardXp();
            xp.load((CompoundTag)t);
            rewardXp.add(xp);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        var list = new ListTag();
        for (GatherItem item : items) {
            var t = new CompoundTag();
            item.save(t);
            list.add(t);
        }
        tag.put(REQUIRED_ITEMS_TAG, list);

        list = new ListTag();
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
    }

    @Override
    protected void make(IQuestDefinition definition) {
        this.type = QuestType.GATHER;

        var random = RandomSource.create();
        makeRequirements(definition, random);
        makeRewards(definition, random);
    }

    protected void makeRequirements(IQuestDefinition definition, RandomSource random) {
        var requirement = definition.randomRequirement(random);
        var requiredItem = requirement.getFirst();
        var requiredSize = requirement.getSecond();
        List<ResourceLocation> values = new ArrayList<>();

        for (Item item : TagHelper.getValues(registry(), tag(requiredItem))) {
            values.add(registry().getKey(item));
        }

        Collections.shuffle(values);

        var amount = requiredSize * definition.level();
        var selection = Math.min(values.size(), random.nextInt(MAX_SELECTION) + 1);

        for (int i = 0; i < selection; i++) {
            var stack = new ItemStack(BuiltInRegistries.ITEM.get(values.get(i)));
            items.add(new GatherItem(stack, amount / selection));
        }
    }

    protected void makeRewards(IQuestDefinition definition, RandomSource random) {
        var reward = definition.randomReward(random);
        var rewardItem = reward.getFirst();
        var rewardSize = reward.getSecond();
        List<ResourceLocation> values = new ArrayList<>();

        for (Item item : TagHelper.getValues(registry(), tag(rewardItem))) {
            values.add(registry().getKey(item));
        }

        Collections.shuffle(values);

        var selection = Math.min(values.size(), MAX_REWARD_ITEMS);
        for (int i = 0; i < selection; i++) {
            var itemId = values.get(i);
            var stack = new ItemStack(BuiltInRegistries.ITEM.get(itemId),
                random.nextIntBetweenInclusive(Math.max(1, rewardSize - 2), rewardSize));

            rewardItems.add(new RewardItem(stack));
        }

        var xp = new RewardXp(definition.experience());
        rewardXp.add(xp);
    }

    public class RewardItem implements Reward {
        static final String ITEM_TAG = "item";

        public ItemStack item;

        public RewardItem() {}

        public RewardItem(ItemStack item) {
            this.item = item;
        }

        @Override
        public RewardType type() {
            return RewardType.ITEM;
        }

        @Override
        public void load(CompoundTag tag) {
            item = ItemStack.of(tag);
        }

        @Override
        public void save(CompoundTag tag) {
            var itemTag = new CompoundTag();
            item.save(itemTag);
            tag.put(ITEM_TAG, itemTag);
        }
    }

    public class RewardXp implements Reward {
        static final String TOTAL_TAG = "total";
        public int total;

        public RewardXp() {}

        public RewardXp(int total) {
            this.total = total;
        }

        @Override
        public RewardType type() {
            return RewardType.EXPERIENCE_LEVEL;
        }

        @Override
        public void load(CompoundTag tag) {
            total = tag.getInt(TOTAL_TAG);
        }

        @Override
        public void save(CompoundTag tag) {
            tag.putInt(TOTAL_TAG, total);
        }
    }

    public class GatherItem implements Requirement {
        static final String ITEM_TAG = "item";
        static final String TOTAL_TAG = "total";

        public ItemStack item;
        public int total;

        public GatherItem() {}

        public GatherItem(ItemStack item, int total) {
            this.item = item;
            this.total = total;
        }

        @Override
        public void load(CompoundTag tag) {
            item = ItemStack.of(tag);
            total = tag.getInt(TOTAL_TAG);
        }

        @Override
        public void save(CompoundTag tag) {
            var itemTag = new CompoundTag();
            item.save(itemTag);
            tag.put(ITEM_TAG, itemTag);
            tag.putInt(TOTAL_TAG, total);
        }

        @Override
        public boolean satisfied() {
            if (player == null) {
                return false;
            }

            return remaining() == 0;
        }

        @Override
        public int total() {
            return total;
        }

        @Override
        public int remaining() {
            if (player == null) {
                return total;
            }

            var remainder = total;
            var inventory = new ArrayList<>(player.getInventory().items);

            for (var invItem : inventory) {
                if (remainder <= 0) continue;

                if (invItem.is(item.getItem())
                    && !invItem.isDamaged()
                    && EnchantmentHelper.getEnchantments(invItem).isEmpty()
                ) {
                    var decrement = Math.min(remainder, invItem.getCount());
                    remainder -= decrement;
                    invItem.shrink(decrement);
                }
            }

            return Math.max(0, remainder);
        }

        @Override
        public void complete() {
            if (player == null) {
                return;
            }

            var remainder = total;

            for (var invItem : player.getInventory().items) {
                if (remainder <= 0) continue;

                if (invItem.is(item.getItem())
                    && !invItem.isDamaged()
                    && EnchantmentHelper.getEnchantments(invItem).isEmpty()
                ) {
                    var decrement = Math.min(remainder, invItem.getCount());
                    remainder -= decrement;
                    invItem.shrink(decrement);
                }
            }
        }
    }
}

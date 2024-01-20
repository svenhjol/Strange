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
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.QuestType;
import svenhjol.strange.feature.quests.Requirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GatherQuest extends Quest<Item> {
    static final int MAX_SELECTION = 3;
    static final String REQUIRED_ITEMS_TAG = "required";

    final List<GatherItem> items = new ArrayList<>();

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
    public void loadAdditional(CompoundTag tag) {
        items.clear();
        var list = tag.getList(REQUIRED_ITEMS_TAG, 10);
        for (Tag t : list) {
            var item = new GatherItem();
            item.load((CompoundTag)t);
            items.add(item);
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
    }

    @Override
    protected void make(QuestDefinition definition, UUID villagerUuid) {
        this.id = makeId();
        this.type = QuestType.GATHER;
        this.status = Status.NOT_STARTED;
        this.epic = definition.isEpic();
        this.villagerProfession = definition.profession();
        this.villagerLevel = definition.level();
        this.villagerUuid = villagerUuid;

        var random = RandomSource.create();
        makeRequirements(definition, random);
        makeRewards(definition, random);
    }

    protected void makeRequirements(QuestDefinition definition, RandomSource random) {
        var requirement = definition.randomRequirement(random);
        var requiredItem = requirement.getFirst();
        var requiredSize = requirement.getSecond();
        List<ResourceLocation> values = new ArrayList<>();

        for (Item item : TagHelper.getValues(registry(), tag(requiredItem))) {
            values.add(registry().getKey(item));
        }

        Collections.shuffle(values);

        var selection = Math.min(values.size(), random.nextInt(MAX_SELECTION) + 1);

        for (int i = 0; i < selection; i++) {
            var stack = new ItemStack(BuiltInRegistries.ITEM.get(values.get(i)));
            items.add(new GatherItem(stack, requiredSize / selection));
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
            item = ItemStack.of(tag.getCompound(ITEM_TAG));
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

            List<ItemStack> inventory = new ArrayList<>();
            for (var stack : player.getInventory().items) {
                inventory.add(stack.copy());
            }

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
        public void start() {
            // no op
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

                    if (!player.getAbilities().instabuild) {
                        invItem.shrink(decrement);
                    }
                }
            }
        }
    }
}

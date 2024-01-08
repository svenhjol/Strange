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
    static final int MAX_REQUIRED_PER_LEVEL = 10;
    static final String ITEMS_TAG = "items";
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
    public List<? extends Criteria> criteria() {
        return items;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        items.clear();
        var list = tag.getList(ITEMS_TAG, 10);
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
        tag.put(ITEMS_TAG, list);
    }

    @Override
    protected void make(IQuestDefinition definition) {
        this.type = QuestType.GATHER;

        var random = RandomSource.create();
        var pool = definition.randomPool(random);
        List<ResourceLocation> values = new ArrayList<>();

        for (Item item : TagHelper.getValues(registry(), tag(pool))) {
            values.add(registry().getKey(item));
        }

        Collections.shuffle(values);

        var amount = MAX_REQUIRED_PER_LEVEL * definition.level();
        var selection = Math.min(values.size(), random.nextInt(MAX_SELECTION) + 1);

        for (int i = 0; i < selection; i++) {
            items.add(new GatherItem(values.get(i), amount / selection));
        }
    }

    public class GatherItem implements Quest.Criteria {
        static final String ITEM_TAG = "item";
        static final String TOTAL_TAG = "total";

        public ResourceLocation item;
        public int total;

        public GatherItem() {
        }

        public GatherItem(ResourceLocation item, int total) {
            this.item = item;
            this.total = total;
        }

        public void load(CompoundTag tag) {
            item = ResourceLocation.tryParse(tag.getString(ITEM_TAG));
            total = tag.getInt(TOTAL_TAG);
        }

        public void save(CompoundTag tag) {
            tag.putString(ITEM_TAG, item.toString());
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
            var stack = registry().get(item);
            var inventory = new ArrayList<>(player.getInventory().items);

            for (var item : inventory) {
                if (remainder <= 0) continue;

                if (item.is(stack)
                    && !item.isDamaged()
                    && EnchantmentHelper.getEnchantments(item).isEmpty()
                ) {
                    var decrement = Math.min(remainder, item.getCount());
                    remainder -= decrement;
                    item.shrink(decrement);
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
            var stack = registry().get(item);

            for (var item : player.getInventory().items) {
                if (remainder <= 0) continue;

                if (item.is(stack)
                    && !item.isDamaged()
                    && EnchantmentHelper.getEnchantments(item).isEmpty()
                ) {
                    var decrement = Math.min(remainder, item.getCount());
                    remainder -= decrement;
                    item.shrink(decrement);
                }
            }
        }
    }
}

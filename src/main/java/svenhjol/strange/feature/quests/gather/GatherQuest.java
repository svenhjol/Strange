package svenhjol.strange.feature.quests.gather;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Requirement;

import java.util.ArrayList;
import java.util.List;

public class GatherQuest extends Quest {
    static final String REQUIRED_ITEMS_TAG = "required";

    final List<GatherItem> items = new ArrayList<>();

    @Override
    public List<? extends Requirement> requirements() {
        return items;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        items.clear();
        var list = tag.getList(REQUIRED_ITEMS_TAG, 10);
        for (var t : list) {
            var item = new GatherItem();
            item.load((CompoundTag)t);
            items.add(item);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        var list = new ListTag();
        for (var item : items) {
            var t = new CompoundTag();
            item.save(t);
            list.add(t);
        }
        tag.put(REQUIRED_ITEMS_TAG, list);
    }

    @Override
    protected void makeRequirements(QuestDefinition definition) {
        var gather = definition.gather().take(random());
        var items = gather.items();

        for (var pair : items) {
            this.items.add(new GatherItem(pair.getFirst(), pair.getSecond()));
        }
    }

    public class GatherItem implements Requirement {
        static final String ITEM_TAG = "item";
        static final String TOTAL_TAG = "total";

        public ItemStack item;
        public int total;

        private GatherItem() {}

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
            return remaining() == 0;
        }

        @Override
        public int total() {
            return total;
        }

        @Override
        public int remaining() {
            if (player == null) {
                return total();
            }

            var remainder = total();

            if (remainder > 0) {
                // Make safe copy of the player's inventory.
                List<ItemStack> inventory = new ArrayList<>();
                for (var stack : player.getInventory().items) {
                    inventory.add(stack.copy());
                }

                for (var invItem : inventory) {
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

                    if (!player.getAbilities().instabuild) {
                        invItem.shrink(decrement);
                    }
                }
            }
        }
    }
}

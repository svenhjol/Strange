package svenhjol.strange.scrolls.tag;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import svenhjol.charm.base.helper.PlayerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Gather implements ISerializable {
    public static final String ITEM_DATA = "item_data";
    public static final String ITEM_COUNT = "item_count";

    private Quest quest;
    private Map<ItemStack, Integer> items = new HashMap<>();
    private Map<ItemStack, Boolean> satisfied = new HashMap<>(); // this is dynamically generated, not stored in nbt

    public Gather(Quest quest) {
        this.quest = quest;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag outTag = new CompoundTag();
        CompoundTag dataTag = new CompoundTag();
        CompoundTag countTag = new CompoundTag();

        if (!items.isEmpty()) {
            int index = 0;
            for (ItemStack stack : items.keySet()) {
                String tagIndex = Integer.toString(index);
                int itemCount = items.get(stack);

                // write the data to the tags at the specified index
                CompoundTag itemTag = new CompoundTag();
                stack.writeNbt(itemTag);
                dataTag.put(tagIndex, itemTag);
                countTag.putInt(tagIndex, itemCount);

                index++;
            }
        }

        outTag.put(ITEM_DATA, dataTag);
        outTag.put(ITEM_COUNT, countTag);
        return outTag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        CompoundTag dataTag = (CompoundTag)tag.get(ITEM_DATA);
        CompoundTag countTag = (CompoundTag)tag.get(ITEM_COUNT);

        items = new HashMap<>();

        if (dataTag != null && dataTag.getSize() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.getSize(); i++) {
                String tagIndex = String.valueOf(i);
                Tag tagAtIndex = dataTag.get(tagIndex);

                if (tagAtIndex == null)
                    continue;

                ItemStack stack = ItemStack.fromNbt((CompoundTag)tagAtIndex);
                int count = Math.max(countTag.getInt(tagIndex), 1);
                items.put(stack, count);
            }
        }
    }

    public void addItem(ItemStack stack) {
        items.put(stack, stack.getCount());
    }

    public Map<ItemStack, Integer> getItems() {
        return items;
    }

    public Map<ItemStack, Boolean> getSatisfied() {
        return satisfied;
    }

    public boolean isSatisfied() {
        if (items.isEmpty())
            return true;

        return satisfied.size() == items.size() && getSatisfied().values().stream().allMatch(r -> r);
    }

    public void complete(PlayerEntity player, MerchantEntity merchant) {
        if (items.isEmpty())
            return;

        items.forEach((stack, count) -> {
            int remainder = count;
            for (ItemStack invStack : PlayerHelper.getInventory(player).main) {
                if (remainder <= 0)
                    continue;

                if (stack.isItemEqualIgnoreDamage(invStack)) {
                    int decrement = Math.min(remainder, invStack.getCount());
                    remainder -= decrement;
                    invStack.decrement(decrement);
                }
            }
        });
    }

    public void update(PlayerEntity player) {
        satisfied.clear();

        Map<ItemStack, Integer> itemsCopy = new HashMap<>(items);
        ArrayList<ItemStack> invCopy = new ArrayList<>(PlayerHelper.getInventory(player).main);

        itemsCopy.forEach((requiredStack, requiredCount) -> {
            int removeIndex = -1;
            int sum = 0;

            for (int i = 0; i < invCopy.size(); i++) {
                ItemStack invStack = invCopy.get(i);
                if (invStack.isEmpty())
                    continue;

                if (requiredStack.isItemEqualIgnoreDamage(invStack)) {
                    sum += invStack.getCount();
                    if (sum >= requiredCount) {
                        satisfied.put(requiredStack, true);
                        removeIndex = i;
                        break;
                    }
                }
            }

            if (removeIndex >= 0) {
                invCopy.remove(removeIndex);
            }
        });
    }
}

package svenhjol.strange.module.scrolls.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.PlayerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Gather implements IQuestSerializable {
    public static final String ITEM_DATA_NBT = "item_data";
    public static final String ITEM_COUNT_NBT = "item_count";

    private final Quest quest;
    private Map<ItemStack, Integer> items = new HashMap<>();

    // dynamically generated, not stored in nbt
    private final Map<ItemStack, Boolean> satisfied = new HashMap<>();

    public Gather(Quest quest) {
        this.quest = quest;
    }

    @Override
    public CompoundTag toNbt() {
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
                stack.save(itemTag);
                dataTag.put(tagIndex, itemTag);
                countTag.putInt(tagIndex, itemCount);

                index++;
            }
        }

        outTag.put(ITEM_DATA_NBT, dataTag);
        outTag.put(ITEM_COUNT_NBT, countTag);
        return outTag;
    }

    @Override
    public void fromNbt(CompoundTag nbt) {
        CompoundTag dataTag = (CompoundTag) nbt.get(ITEM_DATA_NBT);
        CompoundTag countTag = (CompoundTag) nbt.get(ITEM_COUNT_NBT);

        items = new HashMap<>();

        if (dataTag != null && dataTag.size() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.size(); i++) {
                String tagIndex = String.valueOf(i);
                Tag tagAtIndex = dataTag.get(tagIndex);

                if (tagAtIndex == null)
                    continue;

                ItemStack stack = ItemStack.of((CompoundTag)tagAtIndex);
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

    public void complete(Player player, AbstractVillager merchant) {
        if (items.isEmpty())
            return;

        items.forEach((stack, count) -> {
            int remainder = count;
            for (ItemStack invStack : PlayerHelper.getInventory(player).items) {
                if (remainder <= 0)
                    continue;

                if (stack.sameItem(invStack)) {
                    int decrement = Math.min(remainder, invStack.getCount());
                    remainder -= decrement;
                    invStack.shrink(decrement);
                }
            }
        });
    }

    public void update(Player player) {
        satisfied.clear();

        Map<ItemStack, Integer> itemsCopy = new HashMap<>(items);
        ArrayList<ItemStack> invCopy = new ArrayList<>(PlayerHelper.getInventory(player).items);

        itemsCopy.forEach((requiredStack, requiredCount) -> {
            int removeIndex = -1;
            int sum = 0;

            for (int i = 0; i < invCopy.size(); i++) {
                ItemStack invStack = invCopy.get(i);
                if (invStack.isEmpty())
                    continue;

                if (requiredStack.sameItem(invStack)) {
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

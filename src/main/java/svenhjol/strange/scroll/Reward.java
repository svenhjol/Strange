package svenhjol.strange.scroll;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import svenhjol.meson.helper.PlayerHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("SortedCollectionWithNonComparableKeys")
public class Reward implements IScrollSerializable {
    public static final String ITEM_DATA = "item_data";
    public static final String ITEM_COUNT = "item_count";
    public static final String XP_TAG = "xp";

    private int xp;
    private ScrollQuest scrollQuest;
    private Map<ItemStack, Integer> items = new HashMap<>();

    public Reward(ScrollQuest scrollQuest) {
        this.scrollQuest = scrollQuest;
    }

    public void complete(PlayerEntity player) {
        for (ItemStack stack : items.keySet()) {
            int count = items.get(stack);
            ItemStack stackToDrop = stack.copy();

            if (stack.getMaxCount() == 1) {
                for (int i = 0; i < count; i++) {
                    PlayerHelper.addOrDropStack(player, stackToDrop);
                }
            } else {
                stackToDrop.setCount(count);
                PlayerHelper.addOrDropStack(player, stackToDrop);
            }
        }

        if (xp > 0)
            player.addExperienceLevels(xp);
    }

    public CompoundTag toTag() {
        CompoundTag outTag = new CompoundTag();
        CompoundTag dataTag = new CompoundTag();
        CompoundTag countTag = new CompoundTag();

        if (!items.isEmpty()) {
            int index = 0;
            for (ItemStack stack : items.keySet()) {
                String stackIndex = Integer.toString(index);

                CompoundTag itemTag = new CompoundTag();
                stack.toTag(itemTag);
                dataTag.put(stackIndex, itemTag);
                countTag.putInt(stackIndex, items.get(stack));
                index++;
            }
        }

        outTag.put(ITEM_DATA, dataTag);
        outTag.put(ITEM_COUNT, countTag);
        outTag.putInt(XP_TAG, xp);
        return outTag;
    }

    public void fromTag(CompoundTag tag) {
        this.xp = tag.getInt(XP_TAG);

        CompoundTag dataTag = (CompoundTag)tag.get(ITEM_DATA);
        CompoundTag countTag = (CompoundTag)tag.get(ITEM_COUNT);

        this.items = new TreeMap<>();

        if (dataTag != null && dataTag.getSize() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.getSize(); i++) {
                String stackIndex = String.valueOf(i);
                Tag tagAtIndex = dataTag.get(stackIndex);

                if (tagAtIndex == null)
                    continue;

                ItemStack stack = ItemStack.fromTag((CompoundTag)tagAtIndex);
                int count = Math.max(countTag.getInt(stackIndex), 1);
                items.put(stack, count);
            }
        }
    }

    public void addItem(ItemStack stack, int count) {
        this.items.put(stack, count);
    }

    public Map<ItemStack, Integer> getItems() {
        return items;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }
}

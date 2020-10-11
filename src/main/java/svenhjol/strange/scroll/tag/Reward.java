package svenhjol.strange.scroll.tag;

import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public class Reward implements ISerializable {
    public static final String ITEM_DATA = "item_data";
    public static final String ITEM_COUNT = "item_count";
    public static final String LEVELS_TAG = "levels";

    private int levels;
    private Quest quest;
    private Map<ItemStack, Integer> items = new HashMap<>();

    public Reward(Quest quest) {
        this.quest = quest;
    }

    public void complete(PlayerEntity player, MerchantEntity merchant) {
        for (ItemStack stack : items.keySet()) {
            int count = items.get(stack);
            ItemStack stackToDrop = stack.copy();
            stackToDrop.setCount(count);
            LookTargetUtil.give(merchant, stackToDrop, player.getPos());
        }

        if (levels > 0)
            player.addExperienceLevels(levels);
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
        outTag.putInt(LEVELS_TAG, levels);
        return outTag;
    }

    public void fromTag(CompoundTag tag) {
        this.levels = tag.getInt(LEVELS_TAG);

        CompoundTag dataTag = (CompoundTag)tag.get(ITEM_DATA);
        CompoundTag countTag = (CompoundTag)tag.get(ITEM_COUNT);

        this.items = new HashMap<>();

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

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }
}

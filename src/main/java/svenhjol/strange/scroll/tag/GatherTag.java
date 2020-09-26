package svenhjol.strange.scroll.tag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public class GatherTag implements ITag {
    public static final String ITEM_DATA = "item_data";
    public static final String ITEM_COUNT = "item_count";

    private QuestTag questTag;
    private Map<ItemStack, Integer> items = new HashMap<>();
    private Map<ItemStack, Boolean> collected = new HashMap<>(); // this is dynamically generated, not stored in nbt

    public GatherTag(QuestTag questTag) {
        this.questTag = questTag;
    }

    @Override
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
        return outTag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
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

    public Map<ItemStack, Boolean> getCollected() {
        return this.collected;
    }

    public void updateCollected(PlayerEntity player) {
        collected.clear();

        final Map<ItemStack, Integer> sum = new HashMap<>();

        player.inventory.main.forEach(invStack -> {
            if (!invStack.isEmpty()) {
                items.forEach((stack, count) -> {
                    if (stack.isItemEqualIgnoreDamage(invStack))
                        sum.merge(stack, invStack.getCount(), Integer::sum);
                });
            }
        });

        sum.forEach((stack, count) -> collected.put(stack, count >= items.get(stack)));
    }
}

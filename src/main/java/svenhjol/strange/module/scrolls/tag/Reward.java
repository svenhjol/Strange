package svenhjol.strange.module.scrolls.tag;

import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import svenhjol.charm.helper.PlayerHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Reward implements ISerializable {
    public static final String ITEM_DATA = "item_data";
    public static final String ITEM_COUNT = "item_count";
    public static final String PLAYER_XP_TAG = "player_xp";
    public static final String VILLAGER_XP_TAG = "villager_xp";

    private int playerXp;
    private int villagerXp;
    private final Quest quest;
    private Map<ItemStack, Integer> items = new HashMap<>();

    public Reward(Quest quest) {
        this.quest = quest;
    }

    public void complete(PlayerEntity player, @Nullable MerchantEntity merchant) {
        for (ItemStack stack : items.keySet()) {
            int count = items.get(stack);
            ItemStack stackToDrop = stack.copy();
            stackToDrop.setCount(count);

            if (merchant != null) {
                LookTargetUtil.give(merchant, stackToDrop, player.getPos());
            } else {
                PlayerHelper.addOrDropStack(player, stackToDrop);
            }
        }

        if (playerXp > 0)
            player.addExperienceLevels(playerXp);

        // TODO: handle merchant XP
    }

    public NbtCompound toTag() {
        NbtCompound outTag = new NbtCompound();
        NbtCompound dataTag = new NbtCompound();
        NbtCompound countTag = new NbtCompound();

        if (!items.isEmpty()) {
            int index = 0;
            for (ItemStack stack : items.keySet()) {
                String stackIndex = Integer.toString(index);

                NbtCompound itemTag = new NbtCompound();
                stack.writeNbt(itemTag);
                dataTag.put(stackIndex, itemTag);
                countTag.putInt(stackIndex, items.get(stack));
                index++;
            }
        }

        outTag.put(ITEM_DATA, dataTag);
        outTag.put(ITEM_COUNT, countTag);
        outTag.putInt(PLAYER_XP_TAG, playerXp);
        outTag.putInt(VILLAGER_XP_TAG, villagerXp);
        return outTag;
    }

    public void fromTag(NbtCompound tag) {
        this.playerXp = tag.getInt(PLAYER_XP_TAG);
        this.villagerXp = tag.getInt(VILLAGER_XP_TAG);
        NbtCompound dataTag = (NbtCompound)tag.get(ITEM_DATA);
        NbtCompound countTag = (NbtCompound)tag.get(ITEM_COUNT);

        this.items = new HashMap<>();

        if (dataTag != null && dataTag.getSize() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.getSize(); i++) {
                String stackIndex = String.valueOf(i);
                NbtElement tagAtIndex = dataTag.get(stackIndex);

                if (tagAtIndex == null)
                    continue;

                ItemStack stack = ItemStack.fromNbt((NbtCompound)tagAtIndex);
                int count = Math.max(countTag.getInt(stackIndex), 1);
                items.put(stack, count);
            }
        }
    }

    public void addItem(ItemStack stack) {
        this.items.put(stack, stack.getCount());
    }

    public Map<ItemStack, Integer> getItems() {
        return items;
    }

    public int getPlayerXp() {
        return playerXp;
    }

    public int getVillagerXp() {
        return villagerXp;
    }

    public void setPlayerXp(int count) {
        this.playerXp = count;
    }

    public void setVillagerXp(int count) {
        this.villagerXp = count;
    }
}

package svenhjol.strange.module.scrolls.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.PlayerHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Reward implements IQuestSerializable {
    public static final String ITEM_DATA_NBT = "item_data";
    public static final String ITEM_COUNT_NBT = "item_count";
    public static final String PLAYER_XP_NBT = "player_xp";
    public static final String VILLAGER_XP_NBT = "villager_xp";

    private int playerXp;
    private int villagerXp;
    private final Quest quest;
    private Map<ItemStack, Integer> items = new HashMap<>();

    public Reward(Quest quest) {
        this.quest = quest;
    }

    public void complete(Player player, @Nullable AbstractVillager merchant) {
        for (ItemStack stack : items.keySet()) {
            int count = items.get(stack);
            ItemStack stackToDrop = stack.copy();
            stackToDrop.setCount(count);

            if (merchant != null) {
                BehaviorUtils.throwItem(merchant, stackToDrop, player.position());
            } else {
                PlayerHelper.addOrDropStack(player, stackToDrop);
            }
        }

        if (playerXp > 0)
            player.giveExperienceLevels(playerXp);

        // TODO: phase2 handle merchant XP
    }

    public CompoundTag toNbt() {
        CompoundTag outNbt = new CompoundTag();
        CompoundTag dataNbt = new CompoundTag();
        CompoundTag countNbt = new CompoundTag();

        if (!items.isEmpty()) {
            int index = 0;
            for (ItemStack stack : items.keySet()) {
                String stackIndex = Integer.toString(index);

                CompoundTag itemTag = new CompoundTag();
                stack.save(itemTag);
                dataNbt.put(stackIndex, itemTag);
                countNbt.putInt(stackIndex, items.get(stack));
                index++;
            }
        }

        outNbt.put(ITEM_DATA_NBT, dataNbt);
        outNbt.put(ITEM_COUNT_NBT, countNbt);
        outNbt.putInt(PLAYER_XP_NBT, playerXp);
        outNbt.putInt(VILLAGER_XP_NBT, villagerXp);
        return outNbt;
    }

    public void fromNbt(CompoundTag nbt) {
        this.playerXp = nbt.getInt(PLAYER_XP_NBT);
        this.villagerXp = nbt.getInt(VILLAGER_XP_NBT);
        CompoundTag dataTag = (CompoundTag) nbt.get(ITEM_DATA_NBT);
        CompoundTag countTag = (CompoundTag) nbt.get(ITEM_COUNT_NBT);

        this.items = new HashMap<>();

        if (dataTag != null && dataTag.size() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.size(); i++) {
                String stackIndex = String.valueOf(i);
                Tag tagAtIndex = dataTag.get(stackIndex);

                if (tagAtIndex == null)
                    continue;

                ItemStack stack = ItemStack.of((CompoundTag)tagAtIndex);
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

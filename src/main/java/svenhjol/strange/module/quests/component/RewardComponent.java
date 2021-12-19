package svenhjol.strange.module.quests.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.strange.module.quests.IQuestComponent;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.quests.helper.QuestDefinitionHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RewardComponent implements IQuestComponent {
    public static final String TAG_COUNT = "count";
    public static final String TAG_ITEMS = "items";
    public static final String TAG_XP = "xp";
    public static final String TAG_PLAYER = "player";
    public static final String TAG_MERCHANT = "merchant";
    public static final String TAG_ITEM_DATA = "item_data";
    public static final String TAG_ITEM_COUNT = "item_count";
    public static final String TAG_PLAYER_XP = "player_xp";
    public static final String TAG_MERCHANT_XP = "merchant_xp";

    public static final int MAX_ITEM_REWARDS = 2;

    private final Quest quest;

    private Map<ItemStack, Integer> items = new HashMap<>();
    private int playerXp;
    private int merchantXp;

    public RewardComponent(Quest quest) {
        this.quest = quest;
    }

    public String getId() {
        return "reward";
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
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

        if (playerXp > 0) {
            player.giveExperienceLevels(playerXp);
        }
    }

    @Override
    public boolean start(Player player) {
        if (player.level.isClientSide) return false;

        Random random = player.getRandom();
        QuestDefinition definition = quest.getDefinition();
        RewardComponent reward = quest.getComponent(RewardComponent.class);
        float difficulty = quest.getDifficulty();

        Map<String, Map<String, String>> itemDefinition = definition.getReward().getOrDefault(TAG_ITEMS, null);
        Map<String, Map<String, String>> xpDefinition = definition.getReward().getOrDefault(TAG_XP, null);

        if (itemDefinition != null) {
            List<ItemStack> items = QuestDefinitionHelper.parseItems((ServerPlayer)player, itemDefinition, MAX_ITEM_REWARDS, difficulty);
            items.forEach(reward::addItem);
        }

        if (xpDefinition != null) {
            // set the player's awarded XP count
            if (xpDefinition.containsKey(TAG_PLAYER)) {
                Map<String, String> playerXp = xpDefinition.get(TAG_PLAYER);

                int count = QuestDefinitionHelper.getScaledCountFromValue(playerXp.getOrDefault(TAG_COUNT, ""), 0, difficulty, random);
                if (count > 0) {
                    reward.setPlayerXp(count);
                }
            }

            // set the villager's awarded XP count
            if (xpDefinition.containsKey(TAG_MERCHANT)) {
                Map<String, String> merchantXp = xpDefinition.get(TAG_MERCHANT);

                int count = QuestDefinitionHelper.getScaledCountFromValue(merchantXp.getOrDefault(TAG_COUNT, ""), 0, difficulty, random);
                if (count > 0) {
                    reward.setMerchantXp(count);
                }
            }
        }

        return true;
    }

    @Override
    public void update(Player player) {
        // no op
    }

    @Override
    public CompoundTag save() {
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

        outNbt.put(TAG_ITEM_DATA, dataNbt);
        outNbt.put(TAG_ITEM_COUNT, countNbt);
        outNbt.putInt(TAG_PLAYER_XP, playerXp);
        outNbt.putInt(TAG_MERCHANT_XP, merchantXp);

        return outNbt;
    }

    @Override
    public void load(CompoundTag tag) {
        playerXp = tag.getInt(TAG_PLAYER_XP);
        merchantXp = tag.getInt(TAG_MERCHANT_XP);
        CompoundTag dataTag = (CompoundTag) tag.get(TAG_ITEM_DATA);
        CompoundTag countTag = (CompoundTag) tag.get(TAG_ITEM_COUNT);

        items = new HashMap<>();

        if (dataTag != null && dataTag.size() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.size(); i++) {
                String stackIndex = String.valueOf(i);
                Tag tagAtIndex = dataTag.get(stackIndex);

                if (tagAtIndex == null) {
                    continue;
                }

                ItemStack stack = ItemStack.of((CompoundTag)tagAtIndex);
                int count = Math.max(countTag.getInt(stackIndex), 1);
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

    public int getPlayerXp() {
        return playerXp;
    }

    public int getMerchantXp() {
        return merchantXp;
    }

    public void setPlayerXp(int playerXp) {
        this.playerXp = playerXp;
    }

    public void setMerchantXp(int merchantXp) {
        this.merchantXp = merchantXp;
    }
}

package svenhjol.strange.module.scrolls.populator;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.scrolls.nbt.Quest;

import java.util.List;
import java.util.Map;

public class RewardPopulator extends BasePopulator {
    public static final String PLAYER_NBT = "player";
    public static final String VILLAGER_NBT = "villager";
    public static final String XP_NBT = "xp";

    public static final int MAX_ITEM_REWARDS = 3;

    public RewardPopulator(ServerPlayer player, Quest quest) {
        super(player, quest);
    }

    @Override
    public void populate() {
        Map<String, Map<String, Map<String, String>>> reward = definition.getReward();
        if (reward.isEmpty())
            return;

        if (reward.containsKey(ITEMS_NBT)) {
            List<ItemStack> items = parseItems(reward.get(ITEMS_NBT), MAX_ITEM_REWARDS, true);
            items.forEach(quest.getReward()::addItem);
        }

        if (reward.containsKey(XP_NBT)) {
            Map<String, Map<String, String>> xp = reward.get(XP_NBT);

            // set the player's awarded XP count
            if (xp.containsKey(PLAYER_NBT)) {
                Map<String, String> player = xp.get(PLAYER_NBT);

                int count = getCountFromValue(player.getOrDefault(COUNT_NBT, ""), 0, true);
                if (count > 0)
                    quest.getReward().setPlayerXp(count);
            }

            // set the villager's awarded XP count
            if (xp.containsKey(VILLAGER_NBT)) {
                Map<String, String> villager = xp.get(VILLAGER_NBT);

                int count = getCountFromValue(villager.getOrDefault(COUNT_NBT, ""), 0, true);
                if (count > 0)
                    quest.getReward().setVillagerXp(count);
            }
        }
    }
}

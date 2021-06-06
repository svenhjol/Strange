package svenhjol.strange.module.scrolls.populator;

import svenhjol.strange.module.scrolls.tag.Quest;

import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RewardPopulator extends BasePopulator {
    public static final String ITEMS = "items";
    public static final String PLAYER = "player";
    public static final String VILLAGER = "villager";
    public static final String XP = "xp";
    public static final String COUNT = "count";

    public static final int MAX_ITEM_REWARDS = 3;

    public RewardPopulator(ServerPlayer player, Quest quest) {
        super(player, quest);
    }

    @Override
    public void populate() {
        Map<String, Map<String, Map<String, String>>> reward = definition.getReward();
        if (reward.isEmpty())
            return;

        if (reward.containsKey(ITEMS)) {
            List<ItemStack> items = parseItems(reward.get(ITEMS), MAX_ITEM_REWARDS, true);
            items.forEach(quest.getReward()::addItem);
        }

        if (reward.containsKey(XP)) {
            Map<String, Map<String, String>> xp = reward.get(XP);

            // set the player's awarded XP count
            if (xp.containsKey(PLAYER)) {
                Map<String, String> player = xp.get(PLAYER);

                int count = getCountFromValue(player.getOrDefault(COUNT, ""), 0, true);
                if (count > 0)
                    quest.getReward().setPlayerXp(count);
            }

            // set the villager's awarded XP count
            if (xp.containsKey(VILLAGER)) {
                Map<String, String> villager = xp.get(VILLAGER);

                int count = getCountFromValue(villager.getOrDefault(COUNT, ""), 0, true);
                if (count > 0)
                    quest.getReward().setVillagerXp(count);
            }
        }
    }
}

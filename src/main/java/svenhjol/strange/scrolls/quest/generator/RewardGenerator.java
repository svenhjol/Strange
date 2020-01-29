package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Reward;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.*;

public class RewardGenerator extends BaseGenerator
{
    public static final String COUNT = "count";
    public static final String ITEMS = "items";
    public static final String XP = "xp";

    public RewardGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, Map<String, String>> def = definition.getRewards();
        if (def.isEmpty())
            return;

        Condition<Reward> condition = Condition.factory(Reward.class, quest);
        Reward reward = condition.getDelegate();

        if (def.containsKey(ITEMS)) {
            Map<String, String> items = def.get(ITEMS);
            Map<ItemStack, Integer> potentialItems = new HashMap<>();

            for (String stackName : items.keySet()) {
                ItemStack stack = getItemFromKey(stackName);
                if (stack == null)
                    continue;

                int count = getCountFromValue(items.get(stackName), true);
                potentialItems.put(stack, count);
            }

            if (potentialItems.size() > 3) {
                List<ItemStack> keys = new ArrayList<>(potentialItems.keySet());
                Collections.shuffle(keys);
                keys.subList(0, 3).forEach(k -> reward.addItem(k, potentialItems.get(k)));
            } else {
                potentialItems.forEach(reward::addItem);
            }
        }

        if (def.containsKey(XP)) {
            Map<String, String> xp = def.get(XP);
            if (xp.containsKey(COUNT)) {
                int count = getCountFromValue(xp.get(COUNT), true);
                reward.setXP(count);
            }
        }

        quest.getCriteria().addCondition(condition);
    }
}

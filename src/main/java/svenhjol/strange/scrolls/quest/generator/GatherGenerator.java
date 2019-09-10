package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.action.Gather;
import svenhjol.strange.scrolls.quest.iface.IGenerator;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.limit.Time;
import svenhjol.strange.scrolls.quest.reward.XP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatherGenerator implements IGenerator
{
    public IQuest generate(World world, IQuest quest, int tier)
    {
        if (tier == 1) {
            Map<Item, Integer> items = new HashMap<>();
            items.put(Items.COAL, 10);
            items.put(Items.EMERALD, 2);
            items.put(Items.LAPIS_LAZULI, 20);
            items.put(Items.WHEAT, 20);
            items.put(Items.POTATO, 10);
            items.put(Items.CARROT, 10);

            int max = world.rand.nextInt(2) + 1;
            for (int i = 0; i < max; i++) {
                List<Item> keys = new ArrayList<>(items.keySet());
                Item item = keys.get(world.rand.nextInt(keys.size()));
                int count = items.get(item);

                Condition<Gather> action = Condition.factory(Gather.class, quest);
                action.getDelegate()
                    .setStack(new ItemStack(item))
                    .setCount(count);

                quest.getCriteria().addAction(action);
            }

            Condition<Time> limit = Condition.factory(Time.class, quest);
            limit.getDelegate()
                .setLimit(6000);

            quest.getCriteria().addLimit(limit);

            Condition<XP> xp = Condition.factory(XP.class, quest);
            xp.getDelegate().setAmount(1000);

            quest.getCriteria().addReward(xp);
        }

        return quest;
    }
}

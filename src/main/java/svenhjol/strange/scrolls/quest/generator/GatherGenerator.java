package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.scrolls.quest.IGenerator;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.action.Action;

import java.util.*;

public class GatherGenerator implements IGenerator
{
    public IQuest generate(IQuest quest, int tier, Random rand)
    {
        if (tier == 1) {
            Map<Item, Integer> items = new HashMap<>();
            items.put(Items.COAL, 10);
            items.put(Items.EMERALD, 2);
            items.put(Items.LAPIS_LAZULI, 20);
            items.put(Items.WHEAT, 20);
            items.put(Items.POTATO, 10);
            items.put(Items.CARROT, 10);

            int max = rand.nextInt(2) + 1;
            for (int i = 0; i < max; i++) {
                List<Item> keys = new ArrayList<>(items.keySet());
                Item item = keys.get(rand.nextInt(keys.size()));
                int count = items.get(item);

                Action<svenhjol.strange.scrolls.quest.action.Gather> action = Action.factory(svenhjol.strange.scrolls.quest.action.Gather.class, quest);
                action.getDelegate()
                    .setStack(new ItemStack(item))
                    .setCount(count * rand.nextInt(2) + 1);

                quest.getCriteria().addAction(action);
            }
        }

        return quest;
    }
}

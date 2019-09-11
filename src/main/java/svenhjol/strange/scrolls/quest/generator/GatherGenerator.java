package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import svenhjol.strange.Strange;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.action.Gather;
import svenhjol.strange.scrolls.quest.iface.IGenerator;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.limit.Time;
import svenhjol.strange.scrolls.quest.reward.RewardItem;
import svenhjol.strange.scrolls.quest.reward.XP;
import svenhjol.strange.totems.item.TotemOfReturningItem;
import svenhjol.strange.totems.module.TotemOfReturning;
import svenhjol.strange.travelrunes.module.Runestones;
import svenhjol.strange.travelrunes.module.StoneCircles;

import java.util.*;

public class GatherGenerator implements IGenerator
{
    public IQuest generate(World world, IQuest quest, int tier)
    {
        Map<Item, Integer> items = new HashMap<>();
        items.put(Items.COAL, 1);
        items.put(Items.EMERALD, 1);
        items.put(Items.LAPIS_LAZULI, 1);
        items.put(Items.WHEAT, 1);
        items.put(Items.POTATO, 1);
        items.put(Items.CARROT, 1);

        int max = world.rand.nextInt(2) + 1;
        List keys = new ArrayList<>(items.keySet());
        Collections.shuffle(keys);


        int i = 0;
        for (Object key : keys) {
            Item item = (Item)key;
            int count = (int)(items.get(item) * (1.0F + (world.rand.nextFloat())));

            Condition<Gather> gather = Condition.factory(Gather.class, quest);
            gather.getDelegate()
                .setStack(new ItemStack(item))
                .setCount(count);

            quest.getCriteria().addCondition(gather);
            if (++i >= max) break;
        }

        // add a time limit
        Condition<Time> limit = Condition.factory(Time.class, quest);
        limit.getDelegate().setLimit(6000);
        quest.getCriteria().addCondition(limit);

        // add XP
        Condition<XP> rewardXp = Condition.factory(XP.class, quest);
        rewardXp.getDelegate().setAmount(1000000);
        quest.getCriteria().addCondition(rewardXp);

        // add a totem that links to a stone circle
        if (!world.isRemote && Strange.loader.hasModule(TotemOfReturning.class)) {
            ItemStack totem = new ItemStack(TotemOfReturning.item);
            final BlockPos circlePos = world.findNearestStructure(StoneCircles.NAME, Runestones.getOuterPos(world.rand), 1000, true);

            if (circlePos != null) {
                TotemOfReturningItem.setPos(totem, circlePos.add(0, 1, 0));
                totem.setDisplayName(new StringTextComponent("Distant Stone Circle"));
                Condition<RewardItem> rewardItem = Condition.factory(RewardItem.class, quest);
                rewardItem.getDelegate().setStack(totem);
                quest.getCriteria().addCondition(rewardItem);
            }
        }

        return quest;
    }
}

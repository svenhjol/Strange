package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.action.Hunt;
import svenhjol.strange.scrolls.quest.iface.IGenerator;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.*;

public class HuntGenerator implements IGenerator
{
    @Override
    public IQuest generate(World world, IQuest quest, int tier)
    {
        if (tier == 1) {
            Map<ResourceLocation, Integer> targets = new HashMap<>();
            targets.put(new ResourceLocation("spider"), 10);
            targets.put(new ResourceLocation("zombie"), 10);
            targets.put(new ResourceLocation("skeleton"), 10);

            int max = world.rand.nextInt(2) + 1;
            List keys = new ArrayList<>(targets.keySet());
            Collections.shuffle(keys);

            int i = 0;
            for (Object key : keys) {
                ResourceLocation target = (ResourceLocation)key;
                int count = (int)(targets.get(target) * (1.0F + (world.rand.nextFloat())));

                Condition<Hunt> action = Condition.factory(Hunt.class, quest);
                action.getDelegate()
                    .setTarget(target)
                    .setCount(count);

                quest.getCriteria().addAction(action);
                if (++i >= max) break;
            }
        }

        return quest;
    }
}

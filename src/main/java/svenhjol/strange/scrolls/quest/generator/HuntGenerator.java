package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.IGenerator;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.action.Action;
import svenhjol.strange.scrolls.quest.action.Hunt;

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
            for (int i = 0; i < max; i++) {
                List<ResourceLocation> keys = new ArrayList<>(targets.keySet());
                ResourceLocation target = keys.get(world.rand.nextInt(keys.size()));
                int count = targets.get(target);

                Action<Hunt> action = Action.factory(Hunt.class, quest);
                action.getDelegate()
                    .setTarget(target)
                    .setCount(count);

                quest.getCriteria().addAction(action);
            }
        }

        return quest;
    }
}

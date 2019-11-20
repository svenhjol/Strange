package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Hunt;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Map;

public class HuntGenerator extends BaseGenerator
{
    public HuntGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, String> def = definition.getHunt();

        for (String key : def.keySet()) {
            ResourceLocation target = new ResourceLocation(key);
            int count = Integer.parseInt(def.get(key));

            // amount increases based on distance
            count = multiplyDistance(count);

            Condition<Hunt> condition = Condition.factory(Hunt.class, quest);
            condition.getDelegate().setTarget(target).setCount(count);
            quest.getCriteria().addCondition(condition);
        }
    }
}

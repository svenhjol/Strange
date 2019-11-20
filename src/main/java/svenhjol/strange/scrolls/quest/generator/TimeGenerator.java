package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Time;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class TimeGenerator extends BaseGenerator
{
    public TimeGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        int timeLimit = definition.timeLimit;
        if (timeLimit == 0) return;

        Condition<Time> condition = Condition.factory(Time.class, quest);

        int limit = timeLimit * 60 * 20;

        condition.getDelegate().setLimit(limit);
        addCondition(condition);
    }
}

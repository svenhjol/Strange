package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.condition.Time;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class TimeGenerator extends BaseGenerator
{
    public TimeGenerator(World world, IQuest quest, Generator.Definition definition)
    {
        super(world, quest, definition);
    }

    @Override
    public void generate()
    {
        int timeLimit = definition.timeLimit;
        if (timeLimit == 0) return;

        Condition<Time> condition = Condition.factory(Time.class, quest);
        condition.getDelegate().setLimit(timeLimit * 60 * 20);
        addCondition(condition);
    }
}

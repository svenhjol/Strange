package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.condition.XP;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class XPGenerator extends BaseGenerator
{
    public XPGenerator(World world, IQuest quest, Generator.Definition definition)
    {
        super(world, quest, definition);
    }

    @Override
    public void generate()
    {
        int xp = definition.xp;
        if (xp == 0) return;

        Condition<XP> condition = Condition.factory(XP.class, quest);
        condition.getDelegate().setAmount(xp);
        addCondition(condition);
    }
}

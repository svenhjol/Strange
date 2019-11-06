package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.condition.XP;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class XPGenerator extends BaseGenerator
{
    public XPGenerator(World world, BlockPos pos, IQuest quest, Generator.Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        int xp = definition.xp;
        if (xp == 0) return;

        // amount increases based on distance
        xp = multiplyDistance(xp);

        Condition<XP> condition = Condition.factory(XP.class, quest);
        condition.getDelegate().setAmount(xp);
        addCondition(condition);
    }
}

package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public abstract class BaseGenerator
{
    protected World world;
    protected BlockPos pos;
    protected IQuest quest;
    protected Definition definition;

    public BaseGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        this.world = world;
        this.pos = pos;
        this.quest = quest;
        this.definition = definition;
    }

    public void addCondition(Condition condition)
    {
        quest.getCriteria().addCondition(condition);
    }

    public int multiplyValue(int original)
    {
        return (int)(original * quest.getValue());
    }

    public abstract void generate();
}

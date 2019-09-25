package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Generator.Definition;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public abstract class BaseGenerator
{
    protected World world;
    protected IQuest quest;
    protected Definition definition;

    public BaseGenerator(World world, IQuest quest, Definition definition)
    {
        this.world = world;
        this.quest = quest;
        this.definition = definition;
    }

    public void addCondition(Condition condition)
    {
        quest.getCriteria().addCondition(condition);
    }

    public abstract void generate();
}

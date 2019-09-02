package svenhjol.strange.scrolls.quest.condition;

import svenhjol.meson.iface.IMesonEnum;
import svenhjol.strange.scrolls.quest.IQuest;

public class Condition
{
    protected IQuest quest;
    protected Type type;

    public enum Type implements IMesonEnum
    {
        Location
    }

    public Condition(IQuest quest, Type type)
    {
        this.quest = quest;
        this.type = type;
    }

    public Type getType()
    {
        return this.type;
    }

    public boolean isSatisfied()
    {
        return true;
    }
}

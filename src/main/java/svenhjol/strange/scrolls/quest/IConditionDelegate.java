package svenhjol.strange.scrolls.quest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.quest.condition.Condition;

public interface IConditionDelegate
{
    boolean isSatisfied();

    boolean respondTo(Event event);

    Condition.Type getType();

    CompoundNBT toNBT();

    void fromNBT(INBT nbt);

    void setQuest(IQuest quest);
}

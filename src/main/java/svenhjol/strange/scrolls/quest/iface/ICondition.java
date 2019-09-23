package svenhjol.strange.scrolls.quest.iface;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Generator.Definition;

import java.util.List;

public interface ICondition<T extends ICondition>
{
    String getId();

    String getType();

    boolean respondTo(Event event);

    boolean isSatisfied();

    boolean isCompletable();

    float getCompletion();

    CompoundNBT toNBT();

    void fromNBT(INBT nbt);

    void setQuest(IQuest quest);

    List<Condition<T>> fromDefinition(Definition definition);
}

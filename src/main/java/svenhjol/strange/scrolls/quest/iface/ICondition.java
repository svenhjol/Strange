package svenhjol.strange.scrolls.quest.iface;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.eventbus.api.Event;

public interface ICondition
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
}

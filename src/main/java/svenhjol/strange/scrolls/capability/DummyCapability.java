package svenhjol.strange.scrolls.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public final class DummyCapability implements IQuestsCapability
{
    @Override
    public void acceptQuest(PlayerEntity player, IQuest quest)
    {
        // no op
    }

    @Override
    public void removeQuest(PlayerEntity player, IQuest quest)
    {
        // no op
    }

    @Override
    public List<IQuest> getCurrentQuests(PlayerEntity player)
    {
        return null;
    }

    @Override
    public void updateCurrentQuests(PlayerEntity player)
    {
        // no op
    }

    @Override
    public void readNBT(INBT tag)
    {
        // no op
    }

    @Override
    public INBT writeNBT()
    {
        return null;
    }
}

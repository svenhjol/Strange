package svenhjol.strange.scrolls.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public interface IQuestsCapability
{
    void acceptQuest(PlayerEntity player, IQuest quest);

    void removeQuest(PlayerEntity player, IQuest quest);

    List<IQuest> getCurrentQuests(PlayerEntity player);

    void updateCurrentQuests(PlayerEntity player);

    void readNBT(INBT tag);

    INBT writeNBT();
}

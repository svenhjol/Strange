package svenhjol.strange.scrolls.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class QuestEvent extends Event
{
    private IQuest quest;
    private PlayerEntity player;

    public QuestEvent(PlayerEntity player, IQuest quest)
    {
        this.player = player;
        this.quest = quest;
    }

    @Override
    public boolean isCancelable()
    {
        return true;
    }

    public IQuest getQuest()
    {
        return quest;
    }

    public PlayerEntity getPlayer()
    {
        return player;
    }

    public static class Accept extends QuestEvent
    {
        public Accept(PlayerEntity player, IQuest quest)
        {
            super(player, quest);
        }
    }

    public static class Complete extends QuestEvent
    {
        public Complete(PlayerEntity player, IQuest quest)
        {
            super(player, quest);
        }
    }

    public static class Decline extends QuestEvent
    {
        public Decline(PlayerEntity player, IQuest quest)
        {
            super(player, quest);
        }
    }

    public static class Fail extends QuestEvent
    {
        public Fail(PlayerEntity player, IQuest quest)
        {
            super(player, quest);
        }
    }
}

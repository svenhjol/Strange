package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Time implements IDelegate
{
    public final static String ID = "Time";

    private IQuest quest;
    private long start;
    private long lastTime;
    private long limit;

    private final String START = "start";
    private final String LIMIT = "limit";
    private final String LAST_TIME = "lastTime";

    @Override
    public boolean isSatisfied()
    {
        if (start == 0 || lastTime == 0 || limit == 0) return true;
        return lastTime - start < limit;
    }

    @Override
    public boolean isCompletable()
    {
        return false;
    }

    @Override
    public float getCompletion()
    {
        return 0;
    }

    @Override
    public boolean respondTo(Event event, @Nullable PlayerEntity player)
    {
        if (player == null) return false;

        World world = player.world;

        if (event instanceof QuestEvent.Accept) {
            final QuestEvent.Accept qe = (QuestEvent.Accept) event;
            if (qe.getQuest().getId().equals(this.quest.getId())) {
                setStart(world.getGameTime());
                return true;
            }
        }

        if (event instanceof PlayerTickEvent) {
            lastTime = world.getGameTime();

            // check and fail the quest if the time is below zero
            if (getRemaining() <= 0) {
                MinecraftForge.EVENT_BUS.post(new QuestEvent.Fail(player, quest));
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldRemove()
    {
        return false;
    }

    @Override
    public String getType()
    {
        return Criteria.CONSTRAINT;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong(START, start);
        tag.putLong(LIMIT, limit);
        tag.putLong(LAST_TIME, lastTime);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.start = data.getLong(START);
        this.limit = data.getLong(LIMIT);
        this.lastTime = data.getLong(LAST_TIME);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public Time setLimit(long limit)
    {
        this.limit = limit;
        return this;
    }

    public Time setStart(long start)
    {
        this.start = start;
        return this;
    }

    public long getLimit()
    {
        return this.limit;
    }

    public long getRemaining()
    {
        if (this.start == 0) return this.limit;

        return this.limit - (this.lastTime - this.start);
    }
}

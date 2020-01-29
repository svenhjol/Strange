package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.base.helper.QuestHelper;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Hunt implements IDelegate
{
    public final static String ID = "Hunt";

    private IQuest quest;
    private ResourceLocation target;
    private int count;
    private int killed;

    private final String TARGET = "target";
    private final String COUNT = "count";
    private final String KILLED = "killed";

    @Override
    public String getType()
    {
        return Criteria.ACTION;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public boolean respondTo(Event event, @Nullable PlayerEntity player)
    {
        if (event instanceof QuestEvent.Accept) return true; // allow quest to begin with no preconditions

        if (event instanceof LivingDeathEvent) {
            LivingDeathEvent killEvent = (LivingDeathEvent)event;
            LivingEntity killedEntity = killEvent.getEntityLiving();
            if (killedEntity.getEntityString() == null) return false;

            ResourceLocation killedRes = ResourceLocation.tryCreate(killedEntity.getEntityString());
            if (killedRes == null) return false;
            if (!killedRes.equals(this.target)) return false;

            this.killed++;
            if (player != null) {
                showProgress(player);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isSatisfied()
    {
        return count <= killed;
    }

    @Override
    public boolean isCompletable()
    {
        return true;
    }

    @Override
    public float getCompletion()
    {
        int collected = Math.min(this.killed, this.count);
        if (collected == 0 || count == 0) return 0;
        return ((float)collected / (float)count) * 100;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(TARGET, target.toString());
        tag.putInt(KILLED, killed);
        tag.putInt(COUNT, count);
        return tag;
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.target = ResourceLocation.tryCreate(data.getString(TARGET));
        this.count = data.getInt(COUNT);
        this.killed = data.getInt(KILLED);
    }

    @Override
    public boolean shouldRemove()
    {
        return false;
    }

    public Hunt setCount(int count)
    {
        this.count = count;
        return this;
    }

    public Hunt setTarget(ResourceLocation target)
    {
        this.target = target;
        return this;
    }

    public int getKilled()
    {
        return this.killed;
    }

    public int getCount()
    {
        return this.count;
    }

    public ResourceLocation getTarget()
    {
        return this.target;
    }

    private void showProgress(PlayerEntity player)
    {
        if (isSatisfied()) {
            QuestHelper.effectCompleted(player, new TranslationTextComponent("event.strange.quests.hunted_all"));
        } else {
            QuestHelper.effectCounted(player);
        }
    }
}

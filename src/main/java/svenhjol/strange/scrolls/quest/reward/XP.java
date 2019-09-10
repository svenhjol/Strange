package svenhjol.strange.scrolls.quest.reward;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.iface.ICondition;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class XP implements ICondition
{
    public final static String ID = "XP";
    private final String AMOUNT = "amount";

    private IQuest quest;
    private int amount;

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getType()
    {
        return "reward";
    }

    @Override
    public boolean respondTo(Event event)
    {
        if (event instanceof QuestEvent.Complete) {
            QuestEvent qe = (QuestEvent.Complete)event;
            final PlayerEntity player = qe.getPlayer();
            player.giveExperiencePoints(this.getAmount());
            player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

            return true;
        }

        return false;
    }

    @Override
    public boolean isCompleted()
    {
        return false;
    }

    @Override
    public float getCompletion()
    {
        return 0;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(AMOUNT, amount);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.amount = data.getInt(AMOUNT);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }
}

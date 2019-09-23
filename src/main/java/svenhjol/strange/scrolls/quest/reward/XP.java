package svenhjol.strange.scrolls.quest.reward;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.Generator.Definition;
import svenhjol.strange.scrolls.quest.iface.ICondition;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.List;

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
        return Criteria.REWARD;
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
    public boolean isSatisfied()
    {
        return true;
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

    @Override
    public List<Condition<XP>> fromDefinition(Definition definition)
    {
        List<Condition<XP>> out = new ArrayList<>();
        int xp = definition.xp;
        if (xp == 0) return out;

        Condition<XP> reward = Condition.factory(XP.class, quest);
        reward.getDelegate().setAmount(xp);

        out.add(reward);
        return out;
    }
}

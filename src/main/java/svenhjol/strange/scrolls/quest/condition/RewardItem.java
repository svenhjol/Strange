package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class RewardItem implements IDelegate
{
    public final static String ID = "RewardItem";
    private final String STACK = "stack";
    private final String COUNT = "count";

    private IQuest quest;
    private ItemStack stack;
    private int count;

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
            ItemStack stack = this.getStack();
            stack.setCount(this.getCount());
            player.addItemStackToInventory(stack);
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
        tag.put(STACK, stack.serializeNBT());
        tag.putInt(COUNT, count);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.stack = ItemStack.read((CompoundNBT) Objects.requireNonNull(data.get(STACK)));
        this.count = data.getInt(COUNT);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public ItemStack getStack()
    {
        return stack;
    }

    public RewardItem setStack(ItemStack stack)
    {
        this.stack = stack;
        return this;
    }

    public int getCount()
    {
        return this.count;
    }

    public RewardItem setCount(int count)
    {
        this.count = count;
        return this;
    }
}

package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Craft implements IDelegate
{
    public final static String ID = "Craft";

    private IQuest quest;
    private ItemStack stack;
    private int count;
    private int crafted;

    private final String STACK = "stack";
    private final String COUNT = "count";
    private final String CRAFTED = "crafted";

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
    public boolean respondTo(Event event)
    {
        if (isSatisfied()) return false;
        if (crafted >= count) return false;

        if (event instanceof ItemCraftedEvent) {
            ItemCraftedEvent craftedEvent = (ItemCraftedEvent)event;
            ItemStack crafted = craftedEvent.getCrafting();
            if (this.stack == null || crafted.getItem() != stack.getItem().getItem()) return false;

            PlayerEntity player = craftedEvent.getPlayer();
            World world = craftedEvent.getPlayer().world;

            int count = crafted.getCount();
            int remaining = getRemaining();

            if (count > remaining || remaining - 1 == 0) {
                // set the count to the remainder
                crafted.setCount(count - remaining);
                count = remaining;
            } else {
                // cancel the event, don't pick up any items
                craftedEvent.setResult(Event.Result.DENY);
                craftedEvent.setCanceled(true);
            }

            this.crafted += count;

            if (isSatisfied()) {
                Quests.playActionCompleteSound(player);
                player.sendStatusMessage(new TranslationTextComponent("event.strange.quests.crafted_all"), true);
            } else {
                Quests.playActionCountSound(player);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isSatisfied()
    {
        return count <= crafted;
    }

    @Override
    public boolean isCompletable()
    {
        return true;
    }

    @Override
    public float getCompletion()
    {
        int collected = Math.min(this.crafted, this.count);
        if (collected == 0 || count == 0) return 0;
        return ((float)collected / (float)count) * 100;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put(STACK, stack.serializeNBT());
        tag.putInt(CRAFTED, crafted);
        tag.putInt(COUNT, count);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.stack = ItemStack.read((CompoundNBT) Objects.requireNonNull(data.get(STACK)));
        this.count = data.getInt(COUNT);
        this.crafted = data.getInt(CRAFTED);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public Craft setCount(int count)
    {
        this.count = count;
        return this;
    }

    public Craft setStack(ItemStack stack)
    {
        this.stack = stack;
        return this;
    }

    public int getCrafted()
    {
        return this.crafted;
    }

    public int getCount()
    {
        return this.count;
    }

    public int getRemaining()
    {
        return count - crafted;
    }

    public ItemStack getStack()
    {
        return this.stack;
    }
}

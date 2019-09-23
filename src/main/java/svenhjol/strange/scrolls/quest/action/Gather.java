package svenhjol.strange.scrolls.quest.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.Generator.Definition;
import svenhjol.strange.scrolls.quest.iface.ICondition;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Gather implements ICondition
{
    public final static String ID = "Gather";

    private IQuest quest;
    private ItemStack stack;
    private int count;
    private int collected;

    private final String STACK = "stack";
    private final String COUNT = "count";
    private final String COLLECTED = "collected";

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
        if (collected >= count) return false;

        if (event instanceof EntityItemPickupEvent) {
            EntityItemPickupEvent pickupEvent = (EntityItemPickupEvent)event;
            ItemStack pickedUp = pickupEvent.getItem().getItem();

            if (this.stack == null || pickedUp.getItem() != stack.getItem().getItem()) return false;

            PlayerEntity player = pickupEvent.getPlayer();
            World world = pickupEvent.getPlayer().world;
            String notify;

            int count = pickedUp.getCount();
            int remaining = getRemaining();

            if (count > remaining) {
                // set the count to the remainder
                pickedUp.setCount(count - remaining);
                count = remaining;
                notify = "You have collected all the " + stack.getItem().getName().getString() + " required for the quest.";
            } else {
                // cancel the event, don't pick up any items
                pickupEvent.getItem().remove();
                pickupEvent.setResult(Event.Result.DENY);
                pickupEvent.setCanceled(true);
                notify = "Collect " + remaining + " more " + stack.getItem().getName().getString() + ".";
            }

            collected += count;

            if (isSatisfied()) {
                Quests.playActionCompleteSound(player);
            } else {
                Quests.playActionCountSound(player);
            }

            player.sendStatusMessage(new StringTextComponent(notify), true);
            return true;
        }

        return false;
    }

    @Override
    public boolean isSatisfied()
    {
        return count <= collected;
    }

    @Override
    public boolean isCompletable()
    {
        return true;
    }

    @Override
    public float getCompletion()
    {
        int collected = Math.min(this.collected, this.count);
        if (collected == 0 || count == 0) return 0;
        float result = ((float)collected / (float)count) * 100;
        return result;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put(STACK, stack.serializeNBT());
        tag.putInt(COLLECTED, collected);
        tag.putInt(COUNT, count);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.stack = ItemStack.read((CompoundNBT) Objects.requireNonNull(data.get(STACK)));
        this.count = data.getInt(COUNT);
        this.collected = data.getInt(COLLECTED);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public Gather setCount(int count)
    {
        this.count = count;
        return this;
    }

    public Gather setStack(ItemStack stack)
    {
        this.stack = stack;
        return this;
    }

    public int getCollected()
    {
        return this.collected;
    }

    public int getCount()
    {
        return this.count;
    }

    public int getRemaining()
    {
        return count - collected;
    }

    public ItemStack getStack()
    {
        return this.stack;
    }

    public List<Condition<Gather>> fromDefinition(Definition definition)
    {
        List<Condition<Gather>> out = new ArrayList<>();
        Map<String, String> def = definition.getGather();

        for (String key : def.keySet()) {
            ResourceLocation res = new ResourceLocation(key);
            Item item = ForgeRegistries.ITEMS.getValue(res);
            if (item == null) continue;
            int count = definition.parseCount(def.get(key));

            Condition<Gather> condition = Condition.factory(Gather.class, quest);
            condition.getDelegate().setStack(new ItemStack(item)).setCount(count);
            out.add(condition);
        }

        return out;
    }
}

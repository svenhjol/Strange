package svenhjol.strange.scrolls.quest.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.Meson;
import svenhjol.strange.scrolls.quest.IActionDelegate;
import svenhjol.strange.scrolls.quest.IQuest;

import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Gather implements IActionDelegate
{
    private IQuest quest;
    private ItemStack stack;
    private int count;
    private int collected;

    private final String STACK = "stack";
    private final String COUNT = "count";
    private final String COLLECTED = "collected";

    @Override
    public Action.Type getType()
    {
        return Action.Type.Gather;
    }

    @Override
    public boolean respondTo(Event event)
    {
        if (isCompleted()) return false;
        if (collected >= count) return false;

        if (event instanceof EntityItemPickupEvent) {
            EntityItemPickupEvent pickupEvent = (EntityItemPickupEvent)event;
            ItemStack pickedUp = pickupEvent.getItem().getItem();

            if (this.stack == null || pickedUp.getItem() != stack.getItem().getItem()) return false;

            PlayerEntity player = pickupEvent.getPlayer();
            World world = pickupEvent.getPlayer().world;

            int x = player.getPosition().getX();
            int y = player.getPosition().getY();
            int z = player.getPosition().getZ();

            collected += pickedUp.getCount();

            if (isCompleted()) {
                Action.playActionCompleteSound(player);
            } else {
                Action.playActionCountSound(player);
            }

            pickupEvent.getItem().remove();
            pickupEvent.setResult(Event.Result.DENY);
            pickupEvent.setCanceled(true);

            Meson.log("Gathered " + stack + " and now there is " + collected);
            return true;
        }

        return false;
    }

    @Override
    public boolean isCompleted()
    {
        return count <= collected;
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

    public ItemStack getStack()
    {
        return this.stack;
    }
}

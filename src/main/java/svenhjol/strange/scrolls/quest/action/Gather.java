package svenhjol.strange.scrolls.quest.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.Meson;
import svenhjol.strange.scrolls.quest.IActionDelegate;

import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Gather implements IActionDelegate
{
    private Action action;
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

        if (event instanceof EntityItemPickupEvent) {
            EntityItemPickupEvent pickupEvent = (EntityItemPickupEvent)event;
            ItemStack pickedUp = pickupEvent.getItem().getItem();

            if (this.stack == null || pickedUp.getItem() != stack.getItem().getItem()) return false;

            PlayerEntity player = pickupEvent.getPlayer();
            World world = pickupEvent.getPlayer().world;
            collected += pickedUp.getCount();

            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.PLAYERS, 0.3F, 1.0F);

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
    public void setAction(Action action)
    {
        this.action = action;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.stack = ItemStack.read((CompoundNBT) Objects.requireNonNull(data.get(STACK)));
        this.count = data.getInt(COUNT);
        this.collected = data.getInt(COLLECTED);
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

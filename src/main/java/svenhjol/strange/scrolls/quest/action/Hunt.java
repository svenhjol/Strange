package svenhjol.strange.scrolls.quest.action;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.Meson;
import svenhjol.strange.scrolls.quest.IActionDelegate;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Hunt implements IActionDelegate
{
    private Action action;
    private ResourceLocation target;
    private int count;
    private int killed;

    private final String TARGET = "target";
    private final String COUNT = "count";
    private final String KILLED = "killed";

    @Override
    public Action.Type getType()
    {
        return Action.Type.Hunt;
    }

    @Override
    public boolean respondTo(Event event)
    {
        if (isCompleted()) return false;
        if (killed >= count) return false;

        if (event instanceof LivingDeathEvent) {
            LivingDeathEvent killEvent = (LivingDeathEvent)event;
            LivingEntity killedEntity = killEvent.getEntityLiving();
            if (killedEntity.getEntityString() == null) return false;

            ResourceLocation killedRes = ResourceLocation.tryCreate(killedEntity.getEntityString());
            if (killedRes == null) return false;
            if (!killedRes.equals(this.target)) return false;

            // must be a player who did it
            Entity trueSource = killEvent.getSource().getTrueSource();
            if (!(trueSource instanceof PlayerEntity)) return false;

            PlayerEntity player = (PlayerEntity)trueSource;
            World world = player.world;

            this.killed++;

            // TODO proper sound
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.PLAYERS, 0.3F, 1.0F);

            Meson.log("Killed " + killedRes + " and now there is " + this.killed);
            return true;
        }
        return false;
    }

    @Override
    public boolean isCompleted()
    {
        return count <= killed;
    }

    @Override
    public float getCompletion()
    {
        int collected = Math.min(this.killed, this.count);
        if (collected == 0 || count == 0) return 0;
        float result = ((float)collected / (float)count) * 100;
        return result;
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
    public void setAction(Action action)
    {
        this.action = action;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.target = ResourceLocation.tryCreate(data.getString(TARGET));
        this.count = data.getInt(COUNT);
        this.killed = data.getInt(KILLED);
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
}

package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.base.helper.QuestHelper;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class Fetch implements IDelegate
{
    public static final String ID = "Fetch";
    public static final String FETCHED_TAG = "strange:fetched_mob";

    private final int TRIGGER_RANGE = 16;
    private final int VILLAGER_RANGE = 12;

    private final String LOCATION = "location";
    private final String DIM = "dim";
    private final String COUNT = "count";
    private final String TARGET = "target";
    private final String FETCHED = "fetched";

    private IQuest quest;
    private BlockPos location;
    private ResourceLocation target;
    private int fetched;
    private int count;
    private int dim;

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getType()
    {
        return Criteria.ACTION;
    }

    @Override
    public boolean respondTo(Event event, @Nullable PlayerEntity player)
    {
        if (event instanceof QuestEvent.Accept) {
            return onStarted((QuestEvent.Accept) event, player);
        }

        if (event instanceof PlayerTickEvent) {
            return onTick(player);
        }

        return false;
    }

    @Override
    public boolean isSatisfied()
    {
        return count != 0 && count <= fetched;
    }

    @Override
    public boolean isCompletable()
    {
        return true;
    }

    @Override
    public float getCompletion()
    {
        return ((float)fetched / (float)count) * 100;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong(LOCATION, location != null ? location.toLong() : 0);
        tag.putString(TARGET, target.toString());
        tag.putInt(DIM, dim);
        tag.putInt(COUNT, count);
        tag.putInt(FETCHED, fetched);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.location = BlockPos.fromLong(data.getLong(LOCATION));
        this.dim = data.getInt(DIM);
        this.target = ResourceLocation.tryCreate(data.getString(TARGET));
        this.count = data.getInt(COUNT);
        this.fetched = data.getInt(FETCHED);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    @Override
    public boolean shouldRemove()
    {
        return false;
    }

    protected boolean onStarted(QuestEvent.Accept event, PlayerEntity player)
    {
        if (quest.getId().equals(this.quest.getId())) {

            BlockPos pos = QuestHelper.getScrollkeeperNearPlayer(player, quest, VILLAGER_RANGE);
            if (pos != null) {
                this.location = pos;
                return true;
            }
            player.sendStatusMessage(new TranslationTextComponent("event.strange.quests.start_not_at_scrollkeeper"), true);
        }

        return false;
    }

    protected boolean onTick(PlayerEntity player)
    {
        if (player == null) return false;
        World world = player.world;

        if (WorldHelper.getDistanceSq(player.getPosition(), this.location) > TRIGGER_RANGE) return false;

        List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(this.location).grow(VILLAGER_RANGE))
            .stream()
            .filter(m -> m.getType().getRegistryName() != null && m.getType().getRegistryName().equals(this.target))
            .filter(m -> !m.getTags().contains(FETCHED_TAG))
            .collect(Collectors.toList());

        if (!entities.isEmpty()) {
            // add tags so we don't count them again
            for (LivingEntity entity : entities) {
                entity.getTags().add(FETCHED_TAG);
                if (!isSatisfied())
                    this.fetched++;
            }

            if (isSatisfied()) {
                QuestHelper.effectCompleted(player, new TranslationTextComponent("event.strange.quests.fetched_all"));
            } else {
                QuestHelper.effectCounted(player);
            }

            return true;
        }

        return false;
    }

    public Fetch setCount(int count)
    {
        this.count = count;
        return this;
    }

    public Fetch setTarget(ResourceLocation target)
    {
        this.target = target;
        return this;
    }

    public int getFetched()
    {
        return this.fetched;
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

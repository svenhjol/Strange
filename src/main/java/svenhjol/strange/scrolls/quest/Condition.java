package svenhjol.strange.scrolls.quest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;

/**
 * A Condition belongs to a Quest Criteria.
 *
 * A quest can have multiple conditions, each affecting the quest
 * completion criteria, limitations and rewards.
 *
 * Conditions are generated for a quest using the factory method.
 *
 * Event processing is deferred to a Delegate which is attached
 * by the factory method.
 */
public class Condition<T extends IDelegate>
{
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String DATA = "data";
    private static final String PREFIX = "svenhjol.strange.scrolls.quest.condition";

    protected IQuest quest;
    private String type;
    private String id;
    private T delegate;

    public Condition(IQuest quest, T delegate)
    {
        this.quest = quest;
        this.delegate = delegate;
        this.id = delegate.getId();
        this.type = delegate.getType();
        this.delegate.setQuest(quest);
    }

    public String getId()
    {
        return this.id;
    }

    public String getType()
    {
        return this.type;
    }

    public T getDelegate()
    {
        return this.delegate;
    }

    public boolean respondTo(Event event, @Nullable PlayerEntity player)
    {
        if (delegate == null) return false;
        return delegate.respondTo(event, player);
    }

    public IQuest getQuest()
    {
        return this.quest;
    }

    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public boolean isSatisfied()
    {
        if (delegate == null) return true;
        return delegate.isSatisfied();
    }

    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(TYPE, type);
        tag.putString(ID, id);
        tag.put(DATA, this.delegate.toNBT());
        return tag;
    }

    protected void fromNBT(CompoundNBT tag)
    {
        this.id = tag.getString(ID);
        this.type = tag.getString(TYPE);
        this.delegate.fromNBT(tag.get(DATA));
    }

    public static <T extends IDelegate> Condition<T> factory(Class<T> clazz, IQuest quest)
    {
        try {
            T delegate = clazz.getConstructor().newInstance();
            return new Condition<>(quest, delegate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate condition", e);
        }
    }

    public static <T extends IDelegate> Condition<T> factory(CompoundNBT tag, IQuest quest)
    {
        try {
            String id = tag.getString(ID);
            String className = PREFIX + "." + id;

            // noinspection unchecked
            Class<T> clazz = (Class<T>) Class.forName(className);
            Condition<T> condition = factory(clazz, quest);

            condition.fromNBT(tag);
            return condition;

        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate condition", e);
        }
    }
}

package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.iface.IMesonEnum;
import svenhjol.strange.scrolls.quest.IConditionDelegate;
import svenhjol.strange.scrolls.quest.IQuest;

import javax.annotation.Nullable;

public class Condition<T extends IConditionDelegate>
{
    private IQuest quest;
    private Type type;
    private T delegate;

    private static final String TYPE = "type";
    private static final String DATA = "data";
    private static final String PREFIX = "svenhjol.strange.scrolls.quest.condition.";

    public Condition(IQuest quest, T delegate)
    {
        this.quest = quest;
        this.delegate = delegate;
        this.type = delegate.getType();
        this.delegate.setQuest(quest);
    }

    @Nullable
    public Type getType()
    {
        if (delegate == null) return null;
        return delegate.getType();
    }

    public boolean respondTo(Event event)
    {
        if (delegate == null) return false;
        return delegate.respondTo(event);
    }

    public boolean isSatisfied()
    {
        if (delegate == null) return true;
        return delegate.isSatisfied();
    }

    public T getDelegate()
    {
        return this.delegate;
    }

    public static <T extends IConditionDelegate> Condition<T> factory(Class<T> clazz, IQuest quest)
    {
        try {
            T delegate = clazz.getConstructor().newInstance();
            return new Condition<>(quest, delegate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate condition", e);
        }
    }

    public static <T extends IConditionDelegate> Condition<T> factory(CompoundNBT tag, IQuest quest)
    {
        try {
            Condition.Type type = Condition.Type.valueOf(tag.getString(TYPE));
            String className = PREFIX + type.getCapitalizedName();

            // noinspection unchecked
            Class<T> clazz = (Class<T>) Class.forName(className);
            Condition<T> condition = factory(clazz, quest);

            condition.fromNBT(tag);
            return condition;

        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate condition", e);
        }
    }

    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(TYPE, type.toString());
        tag.put(DATA, this.delegate.toNBT());
        return tag;
    }

    private void fromNBT(CompoundNBT tag)
    {
        this.type = Type.valueOf(tag.getString(TYPE));
        this.delegate.fromNBT(tag.get(DATA));
    }

    public enum Type implements IMesonEnum
    {
        Time
    }
}

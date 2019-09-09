package svenhjol.strange.scrolls.quest.action;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.iface.IMesonEnum;
import svenhjol.strange.scrolls.quest.IActionDelegate;
import svenhjol.strange.scrolls.quest.IQuest;

public class Action<T extends IActionDelegate>
{
    private static final String TYPE = "type";
    private static final String DATA = "data";
    private static final String PREFIX = "svenhjol.strange.scrolls.quest.action.";

    private IQuest quest;
    private T delegate;
    private Type type;

    private Action(IQuest quest, T delegate)
    {
        this.quest = quest;
        this.delegate = delegate;
        this.type = delegate.getType();
    }

    public boolean respondTo(Event event)
    {
        if (delegate == null) return false;
        return delegate.respondTo(event);
    }

    public T getDelegate()
    {
        return this.delegate;
    }

    public static <T extends IActionDelegate> Action<T> factory(Class<T> clazz, IQuest quest)
    {
        try {
            T delegate = clazz.getConstructor().newInstance();
            return new Action<>(quest, delegate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate action", e);
        }
    }

    public static <T extends IActionDelegate> Action<T> factory(CompoundNBT tag, IQuest quest)
    {
        try {
            Type type = Type.valueOf(tag.getString(TYPE));
            String className = PREFIX + type.getCapitalizedName();

            // noinspection unchecked
            Class<T> clazz = (Class<T>) Class.forName(className);
            Action<T> action = factory(clazz, quest);

            action.fromNBT(tag);
            return action;

        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate action", e);
        }
    }

    public IQuest getQuest()
    {
        return this.quest;
    }

    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public boolean isCompleted()
    {
        if (delegate == null) return false;
        return delegate.isCompleted();
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
        Gather,
        Hunt
    }
}
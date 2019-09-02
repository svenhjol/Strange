package svenhjol.strange.scrolls.quest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import svenhjol.strange.scrolls.quest.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Criteria
{
    private static final String REWARDS = "rewards";
    private static final String ACTIONS = "actions";
    private static final String CONDITIONS = "conditions";

    private IQuest quest;

    public Criteria(IQuest quest)
    {
        this.quest = quest;
    }

    private List<Action<?>> actions = new ArrayList<>();

    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT actions = new ListNBT();

        for (Action action : this.actions) {
            actions.add(action.toNBT());
        }

        tag.put(ACTIONS, actions);
        return tag;
    }

    public void fromNBT(CompoundNBT tag)
    {
        ListNBT actions = (ListNBT)tag.get(ACTIONS);
        if (actions == null) return;

        this.actions.clear();

        for (INBT nbt : actions) {
            this.actions.add(Action.factory( (CompoundNBT)nbt, quest) );
        }
    }

    public Criteria addAction(Action action)
    {
        this.actions.add(action);
        return this;
    }

    public List<Action<?>> getActions()
    {
        return actions;
    }

    public <T extends IActionDelegate> List<Action<T>> getActions(Class<T> clazz)
    {
        // noinspection unchecked
        return actions.stream()
            .filter(a -> clazz.isInstance(a.getDelegate()))
            .map(a -> (Action<T>) a)
            .collect(Collectors.toList());
    }

    public boolean isCompleted()
    {
        return actions.stream().allMatch(Action::isCompleted);
    }
}

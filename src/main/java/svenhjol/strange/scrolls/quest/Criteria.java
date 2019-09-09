package svenhjol.strange.scrolls.quest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import svenhjol.strange.scrolls.quest.action.Action;
import svenhjol.strange.scrolls.quest.condition.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Criteria
{
    private static final String REWARDS = "rewards";
    private static final String ACTIONS = "actions";
    private static final String CONDITIONS = "conditions";

    private List<Action<?>> actions = new ArrayList<>();
    private List<Condition<?>> conditions = new ArrayList<>();

    private IQuest quest;

    public Criteria(IQuest quest)
    {
        this.quest = quest;
    }

    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT actions = new ListNBT();
        ListNBT conditions = new ListNBT();

        for (Action action : this.actions) {
            actions.add(action.toNBT());
        }
        for (Condition condition : this.conditions) {
            conditions.add(condition.toNBT());
        }

        tag.put(ACTIONS, actions);
        tag.put(CONDITIONS, conditions);
        return tag;
    }

    public void fromNBT(CompoundNBT tag)
    {
        ListNBT actions = (ListNBT)tag.get(ACTIONS);
        if (actions == null) return;

        ListNBT conditions = (ListNBT)tag.get(CONDITIONS);
        if (conditions == null) return;

        this.actions.clear();
        this.conditions.clear();

        for (INBT nbt : actions) {
            this.actions.add(Action.factory( (CompoundNBT)nbt, quest) );
        }
        for (INBT nbt : conditions) {
            this.conditions.add(Condition.factory( (CompoundNBT)nbt, quest) );
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

    public Criteria addCondition(Condition condition)
    {
        // conditions overwrite
        int overwrite = -1;

        for (int i = 0; i < this.conditions.size(); i++) {
            final Condition.Type existingType = this.conditions.get(i).getType();
            final Condition.Type checkType = condition.getType();

            if (existingType == null || checkType == null) continue;
            if (existingType.equals(checkType)) overwrite = i;
        }

        if (overwrite >= 0) {
            this.conditions.set(overwrite, condition);
        } else {
            this.conditions.add(condition);
        }

        return this;
    }

    public List<Condition<?>> getConditions()
    {
        return conditions;
    }

    public <T extends IConditionDelegate> List<Condition<T>> getConditions(Class<T> clazz)
    {
        // noinspection unchecked
        return conditions.stream()
            .filter(c -> clazz.isInstance(c.getDelegate()))
            .map(c -> (Condition<T>) c)
            .collect(Collectors.toList());
    }

    public boolean isSatisfied()
    {
        return conditions.stream().allMatch(Condition::isSatisfied);
    }

    public boolean isCompleted()
    {
        return actions.stream().allMatch(Action::isCompleted);
    }

    public float getCompletion()
    {
        float complete = 0.0F;

        for (Action<?> action : actions) {
            complete += action.getDelegate().getCompletion();
        }

        if (complete == 0.0F) return 0.0F;
        float res = complete / (actions.size() * 100) * 100;
        return res;
    }
}

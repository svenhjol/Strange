package svenhjol.strange.scrolls.quest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import svenhjol.strange.scrolls.quest.iface.ICondition;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedReturnValue")
public class Criteria
{
    private static final String REWARDS = "rewards";
    private static final String ACTIONS = "actions";
    private static final String LIMITS = "limits";

    private List<Condition<?>> actions = new ArrayList<>();
    private List<Condition<?>> limits = new ArrayList<>();
    private List<Condition<?>> rewards = new ArrayList<>();

    private IQuest quest;

    public Criteria(IQuest quest)
    {
        this.quest = quest;
    }

    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT actions = new ListNBT();
        ListNBT limits = new ListNBT();
        ListNBT rewards = new ListNBT();

        for (Condition action : this.actions) {
            actions.add(action.toNBT());
        }
        for (Condition limit : this.limits) {
            limits.add(limit.toNBT());
        }
        for (Condition reward : this.rewards) {
            rewards.add(reward.toNBT());
        }

        tag.put(ACTIONS, actions);
        tag.put(LIMITS, limits);
        tag.put(REWARDS, rewards);
        return tag;
    }

    public void fromNBT(CompoundNBT tag)
    {
        ListNBT actions = (ListNBT)tag.get(ACTIONS);
        ListNBT limits = (ListNBT)tag.get(LIMITS);
        ListNBT rewards = (ListNBT)tag.get(REWARDS);

        this.actions.clear();
        this.limits.clear();

        for (INBT nbt : actions) {
            this.actions.add(Condition.factory( (CompoundNBT)nbt, quest) );
        }
        for (INBT nbt : limits) {
            this.limits.add(Condition.factory( (CompoundNBT)nbt, quest) );
        }
        for (INBT nbt : rewards) {
            this.rewards.add(Condition.factory( (CompoundNBT)nbt, quest) );
        }
    }

    public Criteria addAction(Condition action)
    {
        this.actions.add(action);
        return this;
    }

    public List<Condition<?>> getActions()
    {
        return actions;
    }

    public <T extends ICondition> List<Condition<T>> getActions(Class<T> clazz)
    {
        // noinspection unchecked
        return actions.stream()
            .filter(a -> clazz.isInstance(a.getDelegate()))
            .map(a -> (Condition<T>) a)
            .collect(Collectors.toList());
    }

    public Criteria addReward(Condition reward)
    {
        this.rewards.add(reward);
        return this;
    }

    public List<Condition<?>> getRewards()
    {
        return rewards;
    }

    public <T extends ICondition> List<Condition<T>> getRewards(Class<T> clazz)
    {
        // noinspection unchecked
        return rewards.stream()
            .filter(r -> clazz.isInstance(r.getDelegate()))
            .map(r -> (Condition<T>) r)
            .collect(Collectors.toList());
    }

    public Criteria addLimit(Condition limit)
    {
        // limits overwrite
        int overwrite = -1;

        for (int i = 0; i < this.limits.size(); i++) {
            final String existingType = this.limits.get(i).getType();
            final String checkType = limit.getType();

            if (existingType == null || checkType == null) continue;
            if (existingType.equals(checkType)) overwrite = i;
        }

        if (overwrite >= 0) {
            this.limits.set(overwrite, limit);
        } else {
            this.limits.add(limit);
        }

        return this;
    }

    public List<Condition<?>> getLimits()
    {
        return limits;
    }

    public <T extends ICondition> List<Condition<T>> getLimits(Class<T> clazz)
    {
        // noinspection unchecked
        return limits.stream()
            .filter(c -> clazz.isInstance(c.getDelegate()))
            .map(c -> (Condition<T>) c)
            .collect(Collectors.toList());
    }

    public boolean isCompleted()
    {
        return actions.stream().allMatch(Condition::isCompleted);
    }

    public float getCompletion()
    {
        float complete = 0.0F;

        for (Condition<?> action : actions) {
            complete += action.getDelegate().getCompletion();
        }

        if (complete == 0.0F) return 0.0F;
        float res = complete / (actions.size() * 100) * 100;
        return res;
    }
}

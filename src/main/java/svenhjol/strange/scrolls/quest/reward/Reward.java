package svenhjol.strange.scrolls.quest.reward;

import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.IRewardDelegate;

public class Reward<T extends IRewardDelegate>
{
    private IQuest quest;
    private T delegate;

    private Reward(IQuest quest, T delegate)
    {
        this.quest = quest;
        this.delegate = delegate;
    }


}

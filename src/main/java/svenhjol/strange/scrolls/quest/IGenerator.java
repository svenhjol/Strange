package svenhjol.strange.scrolls.quest;

import java.util.Random;

public interface IGenerator
{
    IQuest generate(IQuest quest, int tier, Random rand);
}

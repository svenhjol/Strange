package svenhjol.strange.scrolls.quest.generator;

import svenhjol.strange.scrolls.module.Quests.QuestType;
import svenhjol.strange.scrolls.quest.IQuest;

import java.util.Random;
import java.util.UUID;

public class QuestGenerator
{
    public static IQuest generate(QuestType type, int tier, UUID seller, Random rand)
    {
        IQuest quest = null;

        switch (type) {
            case Gathering:
                quest = GatheringGenerator.generate(tier, seller, rand);
                break;
        }

        return quest;
    }
}

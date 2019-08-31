package svenhjol.strange.scrolls.client;

import svenhjol.strange.scrolls.quest.IQuest;

import java.util.ArrayList;
import java.util.List;

public class QuestClient
{
    public static List<IQuest> currentQuests = new ArrayList<>();
    public static long lastQuery;
}

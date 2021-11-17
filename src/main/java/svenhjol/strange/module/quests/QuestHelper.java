package svenhjol.strange.module.quests;

import svenhjol.strange.module.quests.exception.QuestException;

import java.util.UUID;

public class QuestHelper {
    public static final UUID ANY_UUID = UUID.fromString("0-0-0-0-0");
    public static final int MAX_QUESTS = 3;

    public static QuestException makeException(Quest quest, String message) {
        Quests.getQuestData().ifPresent(quests -> quests.remove(quest));
        return new QuestException(message);
    }
}

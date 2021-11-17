package svenhjol.strange.module.quests.exception;

import svenhjol.charm.helper.LogHelper;

public class QuestException extends IllegalStateException {
    public QuestException(String message) {
        super(message);
        LogHelper.error(this.getClass(), message);
    }
}

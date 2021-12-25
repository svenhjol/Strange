package svenhjol.strange.module.quests.exception;

import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;

public class QuestException extends IllegalStateException {
    public QuestException(String message) {
        super(message);
        LogHelper.info(Strange.MOD_ID, this.getClass(), message);
    }
}

package svenhjol.strange.module.runes.exception;

import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;

public class RuneStringException extends IllegalStateException {
    public RuneStringException(String message) {
        super(message);
        LogHelper.info(Strange.MOD_ID, getClass(), message);
    }
}

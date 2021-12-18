package svenhjol.strange.module.runes.exception;

import svenhjol.charm.helper.LogHelper;

public class RuneStringException extends IllegalStateException {
    public RuneStringException(String message) {
        super(message);
        LogHelper.error(getClass(), message);
    }
}

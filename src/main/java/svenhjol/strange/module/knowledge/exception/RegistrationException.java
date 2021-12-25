package svenhjol.strange.module.knowledge.exception;

import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;

public class RegistrationException extends IllegalStateException {
    public RegistrationException(String message) {
        super(message);
        LogHelper.info(Strange.MOD_ID, getClass(), message);
    }
}

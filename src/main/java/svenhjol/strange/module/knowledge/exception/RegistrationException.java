package svenhjol.strange.module.knowledge.exception;

import svenhjol.charm.helper.LogHelper;

public class RegistrationException extends IllegalStateException {
    public RegistrationException(String message) {
        super(message);
        LogHelper.error(getClass(), message);
    }
}

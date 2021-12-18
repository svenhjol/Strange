package svenhjol.strange.module.knowledge2.exception;

import svenhjol.charm.helper.LogHelper;

public class RegistrationException extends IllegalStateException {
    public RegistrationException(String message) {
        super(message);
        LogHelper.error(getClass(), message);
    }
}

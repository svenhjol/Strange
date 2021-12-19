package svenhjol.strange.module.bookmarks.exception;

import svenhjol.charm.helper.LogHelper;

public class BookmarkException extends IllegalStateException {
    public BookmarkException(String message) {
        super(message);
        LogHelper.error(getClass(), message);
    }
}

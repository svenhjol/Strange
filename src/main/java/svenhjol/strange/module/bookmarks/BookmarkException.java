package svenhjol.strange.module.bookmarks;

import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;

public class BookmarkException extends IllegalStateException {
    public BookmarkException(String message) {
        super(message);
        LogHelper.info(Strange.MOD_ID, getClass(), message);
    }
}

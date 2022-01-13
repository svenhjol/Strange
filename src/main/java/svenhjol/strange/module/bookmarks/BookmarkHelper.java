package svenhjol.strange.module.bookmarks;

import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Runes;

import java.util.Random;

public class BookmarkHelper {
    public static void setRunesFromBlockPos(Bookmark bookmark, float difficulty) {
        Bookmarks.getBookmarks().ifPresent(bookmarks -> {
            var random = new Random(bookmark.getBlockPos().asLong());
            var runes = RuneHelper.uniqueRunes(bookmarks.branch, random, difficulty, Runes.MIN_PHRASE_LENGTH, Runes.MAX_PHRASE_LENGTH);
            bookmark.setRunes(runes);
        });
    }
}

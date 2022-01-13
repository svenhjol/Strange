package svenhjol.strange.module.bookmarks;

import svenhjol.strange.module.runes.RuneHelper;

import java.util.Random;

public class BookmarkHelper {
    public static void setRunesFromBlockPos(Bookmark bookmark, float difficulty) {
        Bookmarks.getBookmarks().ifPresent(bookmarks -> {
            var length = 19; // 20 when prefixed with start rune
            var random = new Random(bookmark.getBlockPos().asLong());
            var runes = RuneHelper.uniqueRunes(bookmarks.branch, random, difficulty, length, length);
            bookmark.setRunes(runes);
        });
    }
}

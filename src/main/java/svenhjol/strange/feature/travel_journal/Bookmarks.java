package svenhjol.strange.feature.travel_journal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bookmarks {
    public static final String BOOKMARKS_TAG = "bookmarks";
    private List<Bookmark> bookmarks = new ArrayList<>();

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public AddBookmarkResult add(Bookmark bookmark) {
        if (bookmarks.size() > 128) {
            return AddBookmarkResult.FULL;
        }

        if (bookmarks.stream().anyMatch(b -> b.id.equals(bookmark.id))) {
            return AddBookmarkResult.FULL;
        }

        bookmarks.add(bookmark);
        return AddBookmarkResult.SUCCESS;
    }

    public void remove(Bookmark bookmark) {
        var found = bookmarks.stream().filter(b -> b.id.equals(bookmark.id)).findFirst();
        found.ifPresent(b -> bookmarks.remove(b));
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        var list = new ListTag();
        bookmarks.forEach(bookmark -> list.add(bookmark.save()));
        tag.put(BOOKMARKS_TAG, list);
        return tag;
    }

    public static Bookmarks load(CompoundTag tag) {
        var bookmarks = new Bookmarks();
        var list = tag.getList(BOOKMARKS_TAG, 10);

        bookmarks.bookmarks = list.stream()
            .map(t -> Bookmark.load((CompoundTag)t))
            .collect(Collectors.toCollection(ArrayList::new));

        return bookmarks;
    }

    public enum AddBookmarkResult {
        DUPLICATE,
        FULL,
        SUCCESS;
    }
}

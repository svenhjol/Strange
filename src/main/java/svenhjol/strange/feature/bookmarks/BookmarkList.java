package svenhjol.strange.feature.bookmarks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookmarkList {
    public static final String BOOKMARKS_TAG = "bookmarks";
    private List<Bookmark> bookmarks = new ArrayList<>();

    public List<Bookmark> all() {
        return bookmarks;
    }

    public Optional<Bookmark> get(String id) {
        return bookmarks.stream().filter(b -> b.id.equals(id)).findFirst();
    }

    public boolean exists(Bookmark bookmark) {
        return get(bookmark.id).isPresent();
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

    public UpdateBookmarkResult update(Bookmark bookmark) {
        var opt = get(bookmark.id);
        if (opt.isEmpty()) {
            return UpdateBookmarkResult.NOT_FOUND;
        }

        var found = opt.get();
        found.name = bookmark.name;
        found.item = bookmark.item;

        return UpdateBookmarkResult.SUCCESS;
    }

    public DeleteBookmarkResult delete(Bookmark bookmark) {
        var opt = get(bookmark.id);
        if (opt.isEmpty()) {
            return DeleteBookmarkResult.NOT_FOUND;
        }

        bookmarks.remove(opt.get());
        return DeleteBookmarkResult.SUCCESS;
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        var list = new ListTag();
        bookmarks.forEach(bookmark -> list.add(bookmark.save()));
        tag.put(BOOKMARKS_TAG, list);
        return tag;
    }

    public static BookmarkList load(CompoundTag tag) {
        var bookmarks = new BookmarkList();
        var list = tag.getList(BOOKMARKS_TAG, 10);

        bookmarks.bookmarks = list.stream()
            .map(t -> Bookmark.load((CompoundTag)t))
            .collect(Collectors.toCollection(ArrayList::new));

        return bookmarks;
    }

    public enum AddBookmarkResult {
        DUPLICATE,
        FULL,
        SUCCESS
    }

    public enum UpdateBookmarkResult {
        NOT_FOUND,
        SUCCESS
    }

    public enum DeleteBookmarkResult {
        NOT_FOUND,
        SUCCESS;
    }
}

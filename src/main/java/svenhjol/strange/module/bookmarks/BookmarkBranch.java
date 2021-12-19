package svenhjol.strange.module.bookmarks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Tier;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BookmarkBranch extends RuneBranch<Bookmark, Bookmark> {
    public static final String NAME = "Bookmarks";

    @Override
    public Bookmark register(Bookmark bookmark) throws BookmarkException {
        if (!canPlayerAddBookmark(bookmark)) {
            throw new BookmarkException("Player has reached maximum number of bookmarks");
        }

        String runes = bookmark.getRunes();
        add(runes, bookmark);
        return bookmark;
    }

    @Override
    public Tag getValueTag(Bookmark value) {
        return value.save();
    }

    @Override
    public char getStartRune() {
        return RuneHelper.getFromRuneSet(Tier.NOVICE, 1);
    }

    @Override
    public @Nullable String getValueName(Bookmark value) {
        return value.getName();
    }

    @Override
    public String getBranchName() {
        return NAME;
    }

    public List<Bookmark> values(UUID uuid) {
        return values().stream().filter(b -> b.getUuid().equals(uuid)).collect(Collectors.toList());
    }

    private boolean canPlayerAddBookmark(Bookmark bookmark) {
        return values(bookmark.getUuid()).size() <= Bookmarks.maxBookmarksPerPlayer;
    }

    public static BookmarkBranch load(CompoundTag tag) {
        BookmarkBranch branch = new BookmarkBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, Bookmark.load(map.getCompound(runes))));
        return branch;
    }
}

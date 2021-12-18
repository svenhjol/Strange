package svenhjol.strange.module.bookmarks.branch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Tier;

public class BookmarkBranch extends RuneBranch<Bookmark, Bookmark> {
    public static final String NAME = "Bookmarks";

    @Override
    public void register(Bookmark bookmark) {
        String runes = bookmark.getRunes();
        add(runes, bookmark);
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

    public static BookmarkBranch load(CompoundTag tag) {
        BookmarkBranch branch = new BookmarkBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, Bookmark.load(map.getCompound(runes))));
        return branch;
    }
}

package svenhjol.strange.module.bookmarks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.bookmarks.branch.BookmarkBranch;

public class BookmarkData extends SavedData {
    public BookmarkBranch bookmarks;

    public BookmarkData(@Nullable ServerLevel level) {
        this.setDirty();
        bookmarks = new BookmarkBranch();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        bookmarks.save(tag);
        return tag;
    }

    public static BookmarkData load(CompoundTag tag) {
        return load(null, tag);
    }

    public static BookmarkData load(@Nullable ServerLevel level, CompoundTag tag) {
        BookmarkData bookmarks = new BookmarkData(level);
        bookmarks.bookmarks = BookmarkBranch.load(tag);
        return bookmarks;
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange_bookmarks" + dimensionType.getFileSuffix();
    }
}

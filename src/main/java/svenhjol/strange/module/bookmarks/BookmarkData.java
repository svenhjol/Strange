package svenhjol.strange.module.bookmarks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.charm.helper.DimensionHelper;

import javax.annotation.Nullable;

@SuppressWarnings({"unused", "deprecation"})
public class BookmarkData extends SavedData {
    public BookmarkBranch branch;

    public BookmarkData(@Nullable ServerLevel level) {
        setDirty();
        branch = new BookmarkBranch();
    }

    public Bookmark add(Player player, Bookmark bookmark) {
        BookmarkHelper.setRunesFromBlockPos(bookmark, 0.8F);
        branch.register(bookmark);

        setDirty();
        return bookmark;
    }

    public Bookmark add(Player player) throws BookmarkException {
        var bookmark = new Bookmark(player.getUUID(), player.blockPosition(), DimensionHelper.getDimension(player.level));
        branch.register(bookmark);

        setDirty();
        return bookmark;
    }

//    public Bookmark addDeath(Player player, DamageSource source) throws BookmarkException {
//        source.get
//        var bookmarkName = StringHelper.tryResolveLanguageKey(Strange.MOD_ID, "gui.strange.journal.death_bookmark").orElse("Died here");
//
//        var bookmark = new Bookmark(
//            player.getUUID(),
//            bookmarkName,
//            player.blockPosition(),
//            DimensionHelper.getDimension(player.getLevel()),
//            DefaultIcon.DEATH.getId()
//        );
//        bookmark.setAutoPhoto(true);
//        bookmark.setPrivate(true);
//
//        branch.register(bookmark);
//
//        setDirty();
//        return bookmark;
//    }

    public Bookmark update(Bookmark bookmark) throws BookmarkException {
        var runes = bookmark.getRunes();
        var existing = branch.get(runes);
        if (existing == null) {
            throw new BookmarkException("Could not find bookmark matching runes `" + runes + "`");
        }

        existing.setName(bookmark.getName());
        existing.setIcon(bookmark.getIcon());

        setDirty();
        return existing;
    }

    public void remove(Bookmark bookmark) throws BookmarkException {
        var runes = bookmark.getRunes();
        var existing = branch.get(runes);

        if (existing == null) {
            throw new BookmarkException("Could not find bookmark matching runes `" + runes + "`");
        }

        branch.remove(runes);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        branch.save(tag);
        return tag;
    }

    public static BookmarkData load(CompoundTag tag) {
        return load(null, tag);
    }

    public static BookmarkData load(@Nullable ServerLevel level, CompoundTag tag) {
        BookmarkData bookmarks = new BookmarkData(level);
        bookmarks.branch = BookmarkBranch.load(tag);
        return bookmarks;
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange_bookmarks" + dimensionType.getFileSuffix();
    }
}

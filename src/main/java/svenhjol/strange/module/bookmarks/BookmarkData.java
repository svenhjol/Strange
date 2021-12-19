package svenhjol.strange.module.bookmarks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.module.bookmarks.branch.BookmarkBranch;
import svenhjol.strange.module.bookmarks.exception.BookmarkException;

public class BookmarkData extends SavedData {
    public BookmarkBranch bookmarks;

    public BookmarkData(@Nullable ServerLevel level) {
        this.setDirty();
        bookmarks = new BookmarkBranch();
    }

    public Bookmark add(Player player) throws BookmarkException {
        var bookmark = new Bookmark(player.getUUID(), player.blockPosition(), DimensionHelper.getDimension(player.level));
        bookmarks.register(bookmark);

        this.setDirty();
        return bookmark;
    }

    public Bookmark addDeath(Player player) throws BookmarkException {
        var bookmark = new Bookmark(
            player.getUUID(),
            new TranslatableComponent("gui.strange.journal.death_bookmark").getString(),
            player.blockPosition(),
            DimensionHelper.getDimension(player.getLevel()),
            DefaultIcon.DEATH.getId()
        );
        bookmarks.register(bookmark);

        this.setDirty();
        return bookmark;
    }

    public Bookmark update(Bookmark bookmark) throws BookmarkException {
        var runes = bookmark.getRunes();
        var existing = bookmarks.get(runes);
        if (existing == null) {
            throw new BookmarkException("Could not find bookmark matching runes `" + runes + "`");
        }

        existing.setName(bookmark.getName());
        existing.setIcon(bookmark.getIcon());

        this.setDirty();
        return existing;
    }

    public void remove(Bookmark bookmark) throws BookmarkException {
        var runes = bookmark.getRunes();
        var existing = bookmarks.get(runes);

        if (existing == null) {
            throw new BookmarkException("Could not find bookmark matching runes `" + runes + "`");
        }

        bookmarks.remove(runes);
        this.setDirty();
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

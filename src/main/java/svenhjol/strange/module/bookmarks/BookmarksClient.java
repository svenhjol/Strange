package svenhjol.strange.module.bookmarks;

import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.bookmarks.network.*;

import java.util.Optional;

@ClientModule(module = Bookmarks.class)
public class BookmarksClient extends CharmModule {
    private static @Nullable BookmarkBranch branch;

    public static ClientSendCreateBookmark SEND_CREATE_BOOKMARK;
    public static ClientSendRemoveBookmark SEND_REMOVE_BOOKMARK;
    public static ClientSendUpdateBookmark SEND_UPDATE_BOOKMARK;
    public static ClientReceiveBookmarks RECEIVE_BOOKMARKS;
    public static ClientReceiveCreatedBookmark RECEIVE_CREATED_BOOKMARK;
    public static ClientReceiveRemovedBookmark RECEIVE_REMOVED_BOOKMARK;
    public static ClientReceiveUpdatedBookmark RECEIVE_UPDATED_BOOKMARK;
    public static ClientReceiveCreateDeathBookmark RECEIVE_CREATE_DEATH_BOOKMARK;

    @Override
    public void runWhenEnabled() {
        SEND_CREATE_BOOKMARK = new ClientSendCreateBookmark();
        SEND_REMOVE_BOOKMARK = new ClientSendRemoveBookmark();
        SEND_UPDATE_BOOKMARK = new ClientSendUpdateBookmark();
        RECEIVE_BOOKMARKS = new ClientReceiveBookmarks();
        RECEIVE_CREATED_BOOKMARK = new ClientReceiveCreatedBookmark();
        RECEIVE_REMOVED_BOOKMARK = new ClientReceiveRemovedBookmark();
        RECEIVE_UPDATED_BOOKMARK = new ClientReceiveUpdatedBookmark();
        RECEIVE_CREATE_DEATH_BOOKMARK = new ClientReceiveCreateDeathBookmark();
    }

    public static Optional<BookmarkBranch> getBranch() {
        return Optional.ofNullable(branch);
    }

    public static void setBranch(BookmarkBranch branch) {
        BookmarksClient.branch = branch;
    }

    public static ItemStack getBookmarkIconItem(Bookmark bookmark) {
        var icon = bookmark.getIcon();
        return new ItemStack(Registry.ITEM.get(icon));
    }
}

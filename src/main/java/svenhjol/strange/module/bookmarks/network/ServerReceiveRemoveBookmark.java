package svenhjol.strange.module.bookmarks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.BookmarkException;
import svenhjol.strange.module.bookmarks.Bookmarks;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;

/**
 * Server receives a bookmark to be deleted.
 * The bookmark is deleted and then broadcast to all clients so they can keep their copies in sync.
 */
@Id("strange:remove_bookmark")
public class ServerReceiveRemoveBookmark extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        var bookmarks = Bookmarks.getBookmarks().orElseThrow();
        var tag = getCompoundTag(buffer).orElseThrow();

        server.execute(() -> {
            var bookmark = Bookmark.load(tag);

            try {
                // When the bookmark is removed, send it to all players.
                bookmarks.remove(bookmark);
                Bookmarks.SEND_REMOVED_BOOKMARK.sendToAll(server, bookmark);

            } catch (BookmarkException e) {
                LogHelper.warn(getClass(), "Failed to remove bookmark: " + e.getMessage());
            }
        });
    }
}

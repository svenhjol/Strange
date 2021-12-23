package svenhjol.strange.module.bookmarks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.BookmarkException;
import svenhjol.strange.module.bookmarks.Bookmarks;
import svenhjol.strange.network.Id;
import svenhjol.strange.network.ServerReceiver;

/**
 * Server receives an updated bookmark.
 * The bookmark is saved and then broadcast to all clients to keep their copies in sync.
 */
@Id("strange:update_bookmark")
public class ServerReceiveUpdateBookmark extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        var bookmarks = Bookmarks.getBookmarks().orElseThrow();
        var tag = getCompoundTag(buffer).orElseThrow();

        server.execute(() -> {
            var bookmark = Bookmark.load(tag);

            try {
                // When bookmark updated on server, send to all players.
                var updated = bookmarks.update(bookmark);
                Bookmarks.SEND_UPDATED_BOOKMARK.sendToAll(server, updated);

            } catch (BookmarkException e) {
                LogHelper.warn(getClass(), "Failed to update bookmark: " + e.getMessage());
            }
        });
    }
}

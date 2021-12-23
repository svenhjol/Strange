package svenhjol.strange.module.bookmarks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.bookmarks.BookmarkException;
import svenhjol.strange.module.bookmarks.Bookmarks;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;

/**
 * Server receives an empty request to create a new bookmark.
 * The resulting bookmark is sent back to all clients via {@link Bookmarks#SEND_CREATED_BOOKMARK}.
 */
@Id("strange:create_bookmark")
public class ServerReceiveCreateBookmark extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        var bookmarks = Bookmarks.getBookmarks().orElseThrow();

        server.execute(() -> {
            try {
                // When the bookmark has been created on the server side, send it to all connected players.
                var bookmark = bookmarks.add(player);
                Bookmarks.SEND_CREATED_BOOKMARK.sendToAll(server, bookmark);

            } catch (BookmarkException e) {
                LogHelper.warn(getClass(), "Failed to register bookmark: " + e.getMessage());
            }
        });
    }
}

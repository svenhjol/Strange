package svenhjol.strange.module.bookmarks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.BookmarkException;
import svenhjol.strange.module.bookmarks.Bookmarks;
import svenhjol.strange.module.journals.Journals;

/**
 * Server receives an empty request to create a new bookmark.
 * The resulting bookmark is sent back to all clients via {@link Bookmarks#SEND_CREATED_BOOKMARK}.
 */
@Id("strange:create_bookmark")
public class ServerReceiveCreateBookmark extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        var bookmarks = Bookmarks.getBookmarks().orElseThrow();
        var tag = getCompoundTag(buffer).orElseThrow();
        var openImmediately = buffer.readBoolean();

        server.execute(() -> {
            // Do the advancement for the player.
            Journals.triggerMakeBookmark(player);

            try {
                var bookmark = Bookmark.load(tag);
                bookmarks.add(player, bookmark);

                // Pass true for startOpened so that the player who created it sees it straight away.
                Bookmarks.SEND_CREATED_BOOKMARK.sendToAll(server, bookmark, openImmediately);

            } catch (BookmarkException e) {
                LogHelper.warn(getClass(), "Failed to register bookmark: " + e.getMessage());
            }
        });
    }
}

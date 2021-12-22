package svenhjol.strange.module.bookmarks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.bookmarks.BookmarkException;
import svenhjol.strange.module.bookmarks.Bookmarks;
import svenhjol.strange.network.Id;
import svenhjol.strange.network.ServerReceiver;

@Id("strange:create_bookmark")
public class ServerReceiveCreateBookmark extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        var bookmarks = Bookmarks.getBookmarks().orElseThrow();

        server.execute(() -> {
            try {
                var bookmark = bookmarks.add(player);

            } catch (BookmarkException e) {
                LogHelper.warn(getClass(), "Failed to register bookmark: " + e.getMessage());
            }
        });
    }
}

package svenhjol.strange.module.bookmarks.network;

import net.minecraft.server.MinecraftServer;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.network.Id;
import svenhjol.strange.network.ServerSender;

/**
 * Server sends updated bookmark to all connected clients.
 */
@Id("strange:updated_bookmark")
public class ServerSendUpdatedBookmark extends ServerSender {
    public void sendToAll(MinecraftServer server, Bookmark bookmark) {
        sendToAll(server, buf -> buf.writeNbt(bookmark.save()));
    }
}

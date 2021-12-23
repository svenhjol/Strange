package svenhjol.strange.module.bookmarks.network;

import net.minecraft.server.MinecraftServer;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

/**
 * Server sends new bookmark to all connected clients.
 */
@Id("strange:created_bookmark")
public class ServerSendCreatedBookmark extends ServerSender {
    public void sendToAll(MinecraftServer server, Bookmark bookmark) {
        sendToAll(server, buf -> buf.writeNbt(bookmark.save()));
    }
}

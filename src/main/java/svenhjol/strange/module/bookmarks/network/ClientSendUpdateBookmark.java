package svenhjol.strange.module.bookmarks.network;

import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.network.ClientSender;
import svenhjol.strange.network.Id;

/**
 * Client sends an updated bookmark to the server.
 */
@Id("strange:update_bookmark")
public class ClientSendUpdateBookmark extends ClientSender {
    public void send(Bookmark bookmark) {
        send(buf -> buf.writeNbt(bookmark.save()));
    }
}

package svenhjol.strange.module.bookmarks.network;

import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;

/**
 * Clients sends the bookmark to remove from the server.
 */
@Id("strange:remove_bookmark")
public class ClientSendRemoveBookmark extends ClientSender {
    public void send(Bookmark bookmark) {
        send(buf -> buf.writeNbt(bookmark.save()));
    }
}

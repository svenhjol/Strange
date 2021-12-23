package svenhjol.strange.module.bookmarks.network;

import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;

/**
 * Client sends an empty request to create a new bookmark on the server.
 */
@Id("strange:create_bookmark")
public class ClientSendCreateBookmark extends ClientSender {
}

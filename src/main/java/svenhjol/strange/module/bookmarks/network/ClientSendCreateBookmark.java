package svenhjol.strange.module.bookmarks.network;

import svenhjol.strange.network.ClientSender;
import svenhjol.strange.network.Id;

/**
 * Client sends an empty request to create a new bookmark on the server.
 */
@Id("strange:create_bookmark")
public class ClientSendCreateBookmark extends ClientSender {
}

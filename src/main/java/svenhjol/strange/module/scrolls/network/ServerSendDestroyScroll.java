package svenhjol.strange.module.scrolls.network;

import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

/**
 * Server send empty packet to client to do destroy scroll effects
 */
@Id("strange:destroy_scroll")
public class ServerSendDestroyScroll extends ServerSender { }

package svenhjol.strange.module.quests.network;

import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;

/**
 * Client sends request for the server to sync all quests to the client.
 */
@Id("strange:request_quests")
public class ClientSendSyncQuests extends ClientSender {
}

package svenhjol.strange.module.journals.network;

import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

/**
 * Server sends empty packet to tell player that the "open journal" hint should be displayed.
 */
@Id("strange:journal_hint")
public class ServerSendHint extends ServerSender {
}

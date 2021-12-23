package svenhjol.strange.module.scrollkeepers.network;

import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;

/**
 * Client sends empty request to determine satisfied status.
 */
@Id("strange:check_scrollkeeper_satisfied")
public class ClientSendCheckSatisfied extends ClientSender {
    @Override
    protected boolean showDebugMessages() {
        return false;
    }
}

package svenhjol.strange.module.journals.network;

import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.discoveries.Discovery;

@Id("strange:ignore_discovery")
public class ClientSendIgnoreDiscovery extends ClientSender {
    public void send(Discovery discovery) {
        send(buf -> buf.writeNbt(discovery.save()));
    }
}

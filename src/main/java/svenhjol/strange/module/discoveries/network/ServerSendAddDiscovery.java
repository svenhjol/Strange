package svenhjol.strange.module.discoveries.network;

import net.minecraft.server.MinecraftServer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.discoveries.Discovery;

@Id("strange:add_discovery")
public class ServerSendAddDiscovery extends ServerSender {
    public void sendToAll(MinecraftServer server, Discovery discovery) {
        sendToAll(server, buf -> buf.writeNbt(discovery.save()));
    }
}

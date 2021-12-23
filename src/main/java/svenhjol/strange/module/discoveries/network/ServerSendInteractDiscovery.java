package svenhjol.strange.module.discoveries.network;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.discoveries.Discovery;

@Id("strange:interact_discovery")
public class ServerSendInteractDiscovery extends ServerSender {
    public void send(ServerPlayer player, Discovery discovery) {
        send(player, buf -> buf.writeNbt(discovery.save()));
    }
}

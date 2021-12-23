package svenhjol.strange.module.teleport.runic.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.teleport.runic.RunicTeleport;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

@Id("strange:runic_teleport_effect")
public class ServerSendRunicTeleportEffect extends ServerSender {
    public void send(ServerPlayer player, BlockPos origin, RunicTeleport.Type type) {
        send(player, buf -> {
            buf.writeEnum(type);
            buf.writeBlockPos(origin);
        });
    }
}

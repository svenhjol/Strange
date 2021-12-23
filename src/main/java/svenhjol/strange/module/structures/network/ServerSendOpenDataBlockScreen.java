package svenhjol.strange.module.structures.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

@Id("strange:open_data_block_screen")
public class ServerSendOpenDataBlockScreen extends ServerSender {
    public void send(ServerPlayer player, BlockPos pos) {
        super.send(player, buf -> buf.writeBlockPos(pos));
    }
}

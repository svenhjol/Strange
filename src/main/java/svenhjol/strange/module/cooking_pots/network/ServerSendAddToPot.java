package svenhjol.strange.module.cooking_pots.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

@Id("strange:add_to_cooking_pot")
public class ServerSendAddToPot extends ServerSender {
    public void send(ServerPlayer player, BlockPos pos) {
        super.send(player, buf -> buf.writeBlockPos(pos));
    }
}

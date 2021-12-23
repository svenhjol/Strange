package svenhjol.strange.module.ender_bundles.network;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

@Id("strange:updated_ender_inventory")
public class ServerSendUpdatedEnderInventory extends ServerSender {
    @Override
    public void send(ServerPlayer player) {

        super.send(player);
    }
}

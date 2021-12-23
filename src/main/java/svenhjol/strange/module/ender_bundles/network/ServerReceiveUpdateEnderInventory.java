package svenhjol.strange.module.ender_bundles.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;
import svenhjol.strange.module.ender_bundles.EnderBundles;

@Id("strange:update_ender_inventory")
public class ServerReceiveUpdateEnderInventory extends ServerReceiver {
    @Override
    protected boolean showDebugMessages() {
        return false;
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        EnderBundles.SERVER_SEND_UPDATED_ENDER_INVENTORY.send(player);
    }
}

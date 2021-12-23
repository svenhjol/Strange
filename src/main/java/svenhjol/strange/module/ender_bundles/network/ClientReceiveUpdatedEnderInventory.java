package svenhjol.strange.module.ender_bundles.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;

@Id("strange:updated_ender_inventory")
public class ClientReceiveUpdatedEnderInventory extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {

    }
}

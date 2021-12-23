package svenhjol.strange.module.runestones.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;

@Id("strange:runestone_clues")
public class ClientReceiveRunestoneClues extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        // TODO: receive clues from server
    }
}

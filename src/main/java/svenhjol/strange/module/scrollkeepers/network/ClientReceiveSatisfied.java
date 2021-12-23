package svenhjol.strange.module.scrollkeepers.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.scrollkeepers.ScrollkeepersClient;

@Id("strange:scrollkeeper_satisfied")
public class ClientReceiveSatisfied extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var has = buffer.readBoolean();
        client.execute(() -> ScrollkeepersClient.hasSatisfiedQuest = has);
    }

    @Override
    protected boolean showDebugMessages() {
        return false;
    }
}

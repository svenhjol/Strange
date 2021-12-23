package svenhjol.strange.module.discoveries.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.discoveries.DiscoveriesClient;
import svenhjol.strange.module.discoveries.Discovery;

@Id("strange:interact_discovery")
public class ClientReceiveInteractDiscovery extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            var discovery = Discovery.load(tag);
            DiscoveriesClient.setInteractedDiscovery(discovery);
            LogHelper.debug(getClass(), "Interacting with `" + discovery.getRunes() + " : " + discovery.getLocation() + "`.");
        });
    }
}

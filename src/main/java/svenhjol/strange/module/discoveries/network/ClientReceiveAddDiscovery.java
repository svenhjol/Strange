package svenhjol.strange.module.discoveries.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.discoveries.DiscoveriesClient;
import svenhjol.strange.module.discoveries.Discovery;

@Id("strange:add_discovery")
public class ClientReceiveAddDiscovery extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var branch = DiscoveriesClient.getBranch().orElseThrow();
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            var discovery = Discovery.load(tag);
            branch.add(discovery.getRunes(), discovery);
            LogHelper.debug(getClass(), "Added discovery `" + discovery.getRunes() + " : " + discovery.getLocation() + "`.");
        });
    }
}

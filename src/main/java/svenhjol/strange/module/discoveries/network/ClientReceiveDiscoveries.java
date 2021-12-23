package svenhjol.strange.module.discoveries.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.discoveries.DiscoveriesClient;
import svenhjol.strange.module.discoveries.DiscoveryBranch;

@Id("strange:discoveries")
public class ClientReceiveDiscoveries extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            var branch = DiscoveryBranch.load(tag);
            DiscoveriesClient.setBranch(branch);
            LogHelper.debug(getClass(), "Discoveries branch has " + branch.size() + " discoveries.");
        });
    }
}

package svenhjol.strange.module.discoveries;

import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.discoveries.network.ClientReceiveAddDiscovery;
import svenhjol.strange.module.discoveries.network.ClientReceiveDiscoveries;
import svenhjol.strange.module.discoveries.network.ClientReceiveInteractDiscovery;

import javax.annotation.Nullable;
import java.util.Optional;

@ClientModule(module = Discoveries.class)
public class DiscoveriesClient extends CharmModule {
    private static @Nullable DiscoveryBranch branch;
    private static @Nullable Discovery interactedDiscovery;

    public static ClientReceiveDiscoveries CLIENT_RECEIVE_DISCOVERIES;
    public static ClientReceiveAddDiscovery CLIENT_RECEIVE_ADD_DISCOVERY;
    public static ClientReceiveInteractDiscovery CLIENT_RECEIVE_INTERACT_DISCOVERY;

    public static Optional<DiscoveryBranch> getBranch() {
        return Optional.ofNullable(branch);
    }

    public static void setBranch(DiscoveryBranch branch) {
        DiscoveriesClient.branch = branch;
    }

    public static Optional<Discovery> getInteractedDiscovery() {
        return Optional.ofNullable(interactedDiscovery);
    }

    public static void setInteractedDiscovery(@Nullable Discovery discovery) {
        DiscoveriesClient.interactedDiscovery = discovery;
    }

    @Override
    public void runWhenEnabled() {
        CLIENT_RECEIVE_DISCOVERIES = new ClientReceiveDiscoveries();
        CLIENT_RECEIVE_ADD_DISCOVERY = new ClientReceiveAddDiscovery();
        CLIENT_RECEIVE_INTERACT_DISCOVERY = new ClientReceiveInteractDiscovery();
    }
}
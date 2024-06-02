package svenhjol.strange.fabric;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charm.charmony.client.ClientLoader;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeClient;

public final class ClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Launch Charm first!
        svenhjol.charm.fabric.ClientInitializer.initCharm();

        var loader = ClientLoader.create(Strange.ID);
        loader.setup(StrangeClient.features());
        loader.run();
    }
}

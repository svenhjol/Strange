package svenhjol.strange_archaeology.fabric;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charm_core.fabric.base.BaseFabricClientInitializer;
import svenhjol.charm_core.fabric.common.CommonRegistry;
import svenhjol.strange.fabric.FabricModInitializer;
import svenhjol.strange_archaeology.StrangeArchaeologyClient;

public class FabricClientModInitializer implements ClientModInitializer {
    public static final ClientInitializer INIT = new ClientInitializer();
    private StrangeArchaeologyClient mod;

    @Override
    public void onInitializeClient() {
        if (mod == null) {
            // Always init Core Client first.
            svenhjol.charm_core.fabric.FabricClientModInitializer.initCharmCoreClient();

            // Init Charm Client next.
            svenhjol.charm.fabric.FabricClientModInitializer.initCharmClient();

            // Init Strange Client next.
            svenhjol.strange.fabric.FabricClientModInitializer.initStrangeClient();

            mod = new StrangeArchaeologyClient(INIT);
            mod.run();
        }
    }

    public static class ClientInitializer extends BaseFabricClientInitializer {
        @Override
        public String getNamespace() {
            return StrangeArchaeologyClient.MOD_ID;
        }

        @Override
        public CommonRegistry getCommonRegistry() {
            return FabricModInitializer.INIT.getRegistry();
        }
    }
}

package svenhjol.strange.forge;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.charm_core.forge.base.BaseForgeClientInitializer;
import svenhjol.strange.StrangeClient;

public class ForgeClientModInitializer {
    public static final ClientInitializer INIT = new ClientInitializer();
    private final StrangeClient mod;

    public ForgeClientModInitializer() {
        var modEventBus = INIT.getModEventBus();
        modEventBus.addListener(this::handleClientSetup);

        mod = new StrangeClient(INIT);
    }

    private void handleClientSetup(FMLClientSetupEvent event) {
        mod.run();

        // Do final registry tasks.
        event.enqueueWork(INIT.getEvents()::doFinalTasks);
    }

    public static class ClientInitializer extends BaseForgeClientInitializer {
        @Override
        public String getNamespace() {
            return StrangeClient.MOD_ID;
        }
    }
}

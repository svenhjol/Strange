package svenhjol.strange;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.loader.ClientLoader;
import svenhjol.charm.loader.CommonLoader;
import svenhjol.strange.init.StrangeClientParticles;

public class StrangeClient implements ClientModInitializer {
    public static ClientLoader<CharmModule, CommonLoader<CharmModule>> LOADER
        = new ClientLoader<>(Strange.MOD_ID, Strange.LOADER, "svenhjol.strange.module");

    @Override
    public void onInitializeClient() {
        StrangeClientParticles.init();

        LOADER.init();
    }
}

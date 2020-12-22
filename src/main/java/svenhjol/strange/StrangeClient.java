package svenhjol.strange;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charm.base.CharmClientLoader;

public class StrangeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new CharmClientLoader(Strange.MOD_ID);
    }
}

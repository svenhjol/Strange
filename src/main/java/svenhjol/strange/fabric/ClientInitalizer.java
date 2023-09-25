package svenhjol.strange.fabric;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charmony.fabric.CharmonyModLoader;

public class ClientInitalizer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CharmonyModLoader.clientMods("charm", "strange");
    }
}

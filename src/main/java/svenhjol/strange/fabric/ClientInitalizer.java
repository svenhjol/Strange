package svenhjol.strange.fabric;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeClient;

public class ClientInitalizer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        svenhjol.charmony.fabric.ClientInitializer.initCharmony();

        var instance = Mods.client(Strange.ID, StrangeClient::new);
        var loader = instance.loader();

        loader.init(instance.features());
        loader.run();
    }
}

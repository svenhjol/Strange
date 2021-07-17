package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.Charm;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.loader.CommonLoader;

public class Strange implements ModInitializer {
    public static final String MOD_ID = "strange";
    public static CommonLoader<CharmModule> LOADER = new CommonLoader<>(MOD_ID, "svenhjol.strange.module");

    @Override
    public void onInitialize() {
        // always start Charm first
        Charm.init();

        // run Strange's loader after
        LOADER.init();
    }
}
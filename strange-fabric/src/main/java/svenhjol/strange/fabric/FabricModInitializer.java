package svenhjol.strange.fabric;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm_core.fabric.base.BaseFabricInitializer;
import svenhjol.strange.Strange;

public class FabricModInitializer implements ModInitializer {
    private static Strange mod;

    public static final Initializer INIT = new Initializer();

    @Override
    public void onInitialize() {
        initStrange();
    }

    public static void initStrange() {
        if (mod == null) {
            // Always init Core first.
            svenhjol.charm_core.fabric.FabricModInitializer.initCharmCore();

            // Init Charm next.
            svenhjol.charm.fabric.FabricModInitializer.initCharm();

            mod = new Strange(INIT);
            mod.run();
        }
    }

    public static class Initializer extends BaseFabricInitializer {
        @Override
        public String getNamespace() {
            return Strange.MOD_ID;
        }
    }
}

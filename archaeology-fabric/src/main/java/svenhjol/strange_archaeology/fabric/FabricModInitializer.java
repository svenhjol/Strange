package svenhjol.strange_archaeology.fabric;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm_core.fabric.base.BaseFabricInitializer;
import svenhjol.strange_archaeology.StrangeArchaeology;

public class FabricModInitializer implements ModInitializer {
    public static final Initializer INIT = new Initializer();
    private StrangeArchaeology mod;

    @Override
    public void onInitialize() {
        if (mod == null) {
            // Always init Core first.
            svenhjol.charm_core.fabric.FabricModInitializer.initCharmCore();

            // Init Charm next.
            svenhjol.charm.fabric.FabricModInitializer.initCharm();

            // Init Strange next.
            svenhjol.strange.fabric.FabricModInitializer.initStrange();

            mod = new StrangeArchaeology(INIT);
            mod.run();
        }
    }

    public static class Initializer extends BaseFabricInitializer {
        @Override
        public String getNamespace() {
            return StrangeArchaeology.MOD_ID;
        }
    }
}

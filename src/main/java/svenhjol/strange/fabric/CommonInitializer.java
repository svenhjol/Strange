package svenhjol.strange.fabric;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.charmony.common.CommonLoader;
import svenhjol.strange.Strange;

public final class CommonInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        // Launch Charm first!
        svenhjol.charm.fabric.CommonInitializer.initCharm();

        var loader = CommonLoader.create(Strange.ID);
        loader.setup(Strange.features());
        loader.run();
    }
}

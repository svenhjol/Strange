package svenhjol.strange.fabric;

import net.fabricmc.api.ModInitializer;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;

public class Initializer implements ModInitializer {
    @Override
    public void onInitialize() {
        svenhjol.charmony.fabric.Initializer.initCharmony();

        var instance = Mods.common(Strange.ID, Strange::new);
        var loader = instance.loader();

        loader.init(instance.features());
        loader.run();
    }
}

package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.Charm;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.loader.CommonLoader;
import svenhjol.strange.init.StrangeCommands;
import svenhjol.strange.init.StrangeLoot;
import svenhjol.strange.init.StrangeParticles;
import svenhjol.strange.init.StrangeSounds;

public class Strange implements ModInitializer {
    public static final String MOD_ID = "strange";
    public static CommonLoader<CharmModule> LOADER = new CommonLoader<>(MOD_ID, "svenhjol.strange.module");

    @Override
    public void onInitialize() {
        // always start Charm first
        Charm.init();

        // setup Strange afterwards
        StrangeCommands.init();
        StrangeParticles.init();
        StrangeSounds.init();
        StrangeLoot.init();

        LOADER.init();
    }
}
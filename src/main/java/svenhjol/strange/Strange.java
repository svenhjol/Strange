package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.Charm;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.loader.CommonLoader;
import svenhjol.strange.init.StrangeCommands;
import svenhjol.strange.init.StrangeLoot;
import svenhjol.strange.init.StrangeSounds;
import svenhjol.strange.init.StrangeStructures;

public class Strange implements ModInitializer {
    public static final String MOD_ID = "strange";
    public static CommonLoader<CharmModule> LOADER = new CommonLoader<>(MOD_ID, "svenhjol.strange.module");

    @Override
    public void onInitialize() {
        Charm.initCharm();

        StrangeStructures.init();
        StrangeLoot.init();
        StrangeCommands.init();
        StrangeSounds.init();

        LOADER.init();
    }
}

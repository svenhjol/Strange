package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.Charm;
import svenhjol.charm.init.CharmLoader;
import svenhjol.strange.init.StrangeCommands;
import svenhjol.strange.init.StrangeLoot;
import svenhjol.strange.init.StrangeSounds;
import svenhjol.strange.init.StrangeStructures;

public class Strange implements ModInitializer {
    public static final String MOD_ID = "strange";

    @Override
    public void onInitialize() {
        Charm.runFirst();

        new CharmLoader(MOD_ID);

        StrangeStructures.init();
        StrangeLoot.init();
        StrangeCommands.init();
        StrangeSounds.init();
    }
}

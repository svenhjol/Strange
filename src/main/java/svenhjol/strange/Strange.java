package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmLoader;
import svenhjol.strange.module.*;

import java.util.Arrays;

public class Strange implements ModInitializer {
    public static final String MOD_ID = "strange";

    @Override
    public void onInitialize() {
        Charm.runFirst();

        new CharmLoader(MOD_ID, Arrays.asList(
            Astrolabes.class,
            Mobs.class,
            Rubble.class,
            Ruins.class,
            RunePortals.class,
            Runestones.class,
            Scrollkeepers.class,
            Scrolls.class,
            StoneCircles.class,
            TravelJournals.class,
            TotemOfFlying.class,
            Treasure.class
        ));

        StrangeStructures.init();
        StrangeLoot.init();
        StrangeCommands.init();
        StrangeSounds.init();
    }
}

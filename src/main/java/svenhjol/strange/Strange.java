package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmLoader;
import svenhjol.strange.module.Astrolabes;
import svenhjol.strange.module.Mobs;
import svenhjol.strange.module.Rubble;
import svenhjol.strange.module.Ruins;
import svenhjol.strange.module.RunePortals;
import svenhjol.strange.module.Runestones;
import svenhjol.strange.module.Scrollkeepers;
import svenhjol.strange.module.Scrolls;
import svenhjol.strange.module.StoneCircles;
import svenhjol.strange.storagecrates.StorageCrates;
import svenhjol.strange.module.TotemOfFlying;
import svenhjol.strange.module.TravelJournals;
import svenhjol.strange.module.Treasure;

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
            Treasure.class,
            StorageCrates.class
        ));

        StrangeStructures.init();
        StrangeLoot.init();
        StrangeCommands.init();
        StrangeSounds.init();
    }
}

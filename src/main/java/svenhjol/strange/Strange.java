package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmLoader;
import svenhjol.strange.astrolabes.Astrolabes;
import svenhjol.strange.base.StrangeCommands;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.base.StrangeStructures;
import svenhjol.strange.mobs.Mobs;
import svenhjol.strange.rubble.Rubble;
import svenhjol.strange.ruins.Ruins;
import svenhjol.strange.runeportals.RunePortals;
import svenhjol.strange.runestones.Runestones;
import svenhjol.strange.scrollkeepers.Scrollkeepers;
import svenhjol.strange.scrolls.Scrolls;
import svenhjol.strange.stonecircles.StoneCircles;
import svenhjol.strange.storagecrates.StorageCrates;
import svenhjol.strange.totems.TotemOfFlying;
import svenhjol.strange.totems.TotemOfPreserving;
import svenhjol.strange.traveljournals.TravelJournals;
import svenhjol.strange.treasure.Treasure;

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
            TotemOfPreserving.class,
            Treasure.class,
            StorageCrates.class
        ));

        StrangeStructures.init();
        StrangeLoot.init();
        StrangeCommands.init();
        StrangeSounds.init();
    }
}

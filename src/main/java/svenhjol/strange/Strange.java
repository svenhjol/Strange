package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.strange.base.StrangeCommands;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.base.StrangeStructures;
import svenhjol.strange.excavation.Excavation;
import svenhjol.strange.foundations.Foundations;
import svenhjol.strange.legendaryitems.LegendaryItems;
import svenhjol.strange.ruins.Ruins;
import svenhjol.strange.runestones.Runestones;
import svenhjol.strange.runicaltars.RunicAltars;
import svenhjol.strange.runicfragments.RunicFragments;
import svenhjol.strange.scrollkeepers.Scrollkeepers;
import svenhjol.strange.scrolls.Scrolls;
import svenhjol.strange.stonecircles.StoneCircles;
import svenhjol.strange.totems.TotemOfPreserving;
import svenhjol.strange.writingdesks.WritingDesks;

import java.util.Arrays;

public class Strange implements ModInitializer {
    public static final String MOD_ID = "strange";

    @Override
    public void onInitialize() {
        StrangeStructures.init();
        StrangeLoot.init();
        StrangeCommands.init();

        ModuleHandler.INSTANCE.registerFabricMod(MOD_ID, Arrays.asList(
            Excavation.class,
            Foundations.class,
            LegendaryItems.class,
            Ruins.class,
            Runestones.class,
            RunicAltars.class,
            RunicFragments.class,
            Scrollkeepers.class,
            Scrolls.class,
            StoneCircles.class,
            TotemOfPreserving.class,
            WritingDesks.class
        ));
    }
}

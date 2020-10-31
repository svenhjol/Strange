package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.base.StrangeStructures;
import svenhjol.strange.module.*;
import svenhjol.strange.writingdesks.WritingDesks;

import java.util.ArrayList;
import java.util.Arrays;

public class Strange implements ModInitializer {
    public static final String MOD_ID = "strange";

    @Override
    public void onInitialize() {
        StrangeStructures.init();
        StrangeLoot.init();

        ModuleHandler.AVAILABLE_MODULES.put(Strange.MOD_ID, new ArrayList<>(Arrays.asList(
            Excavation.class,
            Foundations.class,
            LegendaryItems.class,
            Ruins.class,
            Runestones.class,
            RunicTablets.class,
            Scrollkeepers.class,
            Scrolls.class,
            StoneCircles.class,
            TotemOfPreserving.class,
            WritingDesks.class
        )));
    }
}

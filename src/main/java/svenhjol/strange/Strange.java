package svenhjol.strange;

import net.fabricmc.api.ModInitializer;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.strange.base.StrangeStructures;
import svenhjol.strange.module.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Strange implements ModInitializer {
    public static final String MOD_ID = "strange";

    @Override
    public void onInitialize() {
        StrangeStructures.init();

        ModuleHandler.AVAILABLE_MODULES.put(Strange.MOD_ID, new ArrayList<>(Arrays.asList(
            EntitySpawner.class,
            Excavation.class,
            Ruins.class,
            Runestones.class,
            RunicTablets.class,
            Scrollkeepers.class,
            Scrolls.class,
            StoneCircles.class
        )));
    }
}

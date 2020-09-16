package svenhjol.strange;

import svenhjol.meson.MesonMod;
import svenhjol.meson.MesonModule;
import svenhjol.strange.base.StrangeStructures;
import svenhjol.strange.module.*;

import java.util.Arrays;
import java.util.List;

public class Strange extends MesonMod {
    public static final String MOD_ID = "strange";

    @Override
    public void onInitialize() {
        super.init(MOD_ID);
        StrangeStructures.init(this);
    }

    @Override
    protected List<Class<? extends MesonModule>> getModules() {
        return Arrays.asList(
            Amethyst.class,
            EntitySpawner.class,
            Ruins.class,
            Runestones.class,
            Scrollkeepers.class,
            StoneCircles.class
        );
    }
}

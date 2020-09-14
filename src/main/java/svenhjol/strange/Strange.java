package svenhjol.strange;

import svenhjol.meson.MesonMod;
import svenhjol.meson.MesonModule;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.module.Amethyst;
import svenhjol.strange.module.Runestones;
import svenhjol.strange.module.StoneCircles;

import java.util.Arrays;
import java.util.List;

public class Strange extends MesonMod {
    public static final String MOD_ID = "strange";

    @Override
    public void onInitialize() {
        super.init(MOD_ID);
        StrangeSounds.init(this);
    }

    @Override
    protected List<Class<? extends MesonModule>> getModules() {
        return Arrays.asList(
            Amethyst.class,
            Runestones.class,
            StoneCircles.class
        );
    }
}

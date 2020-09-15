package svenhjol.strange;

import svenhjol.meson.MesonMod;
import svenhjol.meson.MesonModule;
import svenhjol.meson.event.LoadWorldCallback;
import svenhjol.strange.base.StrangeStructures;
import svenhjol.strange.helper.DecorationHelper;
import svenhjol.strange.module.*;

import java.util.Arrays;
import java.util.List;

public class Strange extends MesonMod {
    public static final String MOD_ID = "strange";

    @Override
    public void onInitialize() {
        super.init(MOD_ID);
        StrangeStructures.init(this);

        // TODO: move to Charm
        LoadWorldCallback.EVENT.register(server -> {
            DecorationHelper.init();
        });
    }

    @Override
    protected List<Class<? extends MesonModule>> getModules() {
        return Arrays.asList(
            Amethyst.class,
            EntitySpawner.class,
            Ruins.class,
            Runestones.class,
            StoneCircles.class
        );
    }
}

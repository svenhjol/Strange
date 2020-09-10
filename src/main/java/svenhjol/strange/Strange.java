package svenhjol.strange;

import svenhjol.meson.MesonMod;
import svenhjol.meson.MesonModule;

import java.util.Arrays;
import java.util.List;

public class Strange extends MesonMod {
    public static final String MOD_ID = "strange";

    @Override
    public void onInitialize() {
        super.init(MOD_ID);
    }

    @Override
    protected List<Class<? extends MesonModule>> getModules() {
        return Arrays.asList();
    }
}

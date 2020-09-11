package svenhjol.strange.module;

import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.block.AmethystBlock;

@Module(description = "Decorative block found in the End Islands.")
public class Amethyst extends MesonModule {
    public static AmethystBlock AMETHYST;

    @Override
    public void register() {
        AMETHYST = new AmethystBlock(this);
    }
}

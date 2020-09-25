package svenhjol.strange.module;

import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.block.AmethystBlock;
import svenhjol.strange.block.AmethystStairsBlock;

@Module(description = "Decorative block found in the End Islands.")
public class Amethyst extends MesonModule {
    public static AmethystBlock AMETHYST;
    public static AmethystStairsBlock AMETHYST_STAIRS;

    @Override
    public void register() {
        AMETHYST = new AmethystBlock(this);
        AMETHYST_STAIRS = new AmethystStairsBlock(this);
    }
}

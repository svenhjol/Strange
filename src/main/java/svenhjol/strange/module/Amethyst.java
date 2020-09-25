package svenhjol.strange.module;

import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.block.AmethystBlock;
import svenhjol.strange.block.AmethystStairsBlock;
import svenhjol.strange.block.AmethystSlabBlock;
import svenhjol.strange.block.AmethystWallBlock;

@Module(description = "Decorative block found in the End Islands.")
public class Amethyst extends MesonModule {
    public static AmethystBlock AMETHYST;
    public static AmethystStairsBlock AMETHYST_STAIRS;
    public static AmethystWallBlock AMETHYST_WALL;
    public static AmethystSlabBlock AMETHYST_SLAB;

    @Override
    public void register() {
        AMETHYST = new AmethystBlock(this);
        AMETHYST_STAIRS = new AmethystStairsBlock(this);
        AMETHYST_WALL = new AmethystWallBlock(this);
        AMETHYST_SLAB = new AmethystSlabBlock(this);
    }
}

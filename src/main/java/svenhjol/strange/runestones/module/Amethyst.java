package svenhjol.strange.runestones.module;

import svenhjol.charm.base.CharmCategories;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.block.*;

@Module(mod = Strange.MOD_ID, category = CharmCategories.DECORATION)
public class Amethyst extends MesonModule {
    public static AmethystBlock block;
    public static PolishedAmethystBlock polishedBlock;
    public static PolishedAmethystSlabBlock polishedSlabBlock;
    public static PolishedAmethystStairsBlock polishedStairsBlock;
    public static PolishedAmethystWallBlock polishedWallBlock;

    @Override
    public void init() {
        block = new AmethystBlock(this);
        polishedBlock = new PolishedAmethystBlock(this);
        polishedSlabBlock = new PolishedAmethystSlabBlock(this);
        polishedStairsBlock = new PolishedAmethystStairsBlock(this);
        polishedWallBlock = new PolishedAmethystWallBlock(this);

        // amethyst rune portal blocks are instantiated by RunePortals module
    }
}

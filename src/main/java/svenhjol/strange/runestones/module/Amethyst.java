package svenhjol.strange.runestones.module;

import svenhjol.charm.base.CharmCategories;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.block.AmethystBlock;
import svenhjol.strange.runestones.block.PolishedAmethystBlock;

@Module(mod = Strange.MOD_ID, category = CharmCategories.DECORATION)
public class Amethyst extends MesonModule {
    public static AmethystBlock block;
    public static PolishedAmethystBlock polishedBlock;

    @Override
    public void init() {
        block = new AmethystBlock(this);
        polishedBlock = new PolishedAmethystBlock(this);

        // amethyst rune portal blocks are instantiated by RunePortals module
    }
}

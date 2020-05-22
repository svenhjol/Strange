package svenhjol.strange.decoration.module;

import svenhjol.charm.base.CharmCategories;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.decoration.block.AmethystBlock;

@Module(mod = Strange.MOD_ID, category = CharmCategories.DECORATION)
public class Amethyst extends MesonModule {
    public static AmethystBlock block;

    @Override
    public void init() {
        block = new AmethystBlock(this);
    }
}

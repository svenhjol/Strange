package svenhjol.strange.module.rune_portals;

import svenhjol.charm.block.CharmBlock;
import svenhjol.charm.module.CharmModule;

public abstract class BaseFrameBlock extends CharmBlock {
    public BaseFrameBlock(CharmModule module, String name, Settings props, String... loadedMods) {
        super(module, name, props, loadedMods);
    }
}

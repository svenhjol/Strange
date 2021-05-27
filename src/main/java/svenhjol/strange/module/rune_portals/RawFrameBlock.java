package svenhjol.strange.module.rune_portals;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import svenhjol.charm.module.CharmModule;

public class RawFrameBlock extends BaseFrameBlock {
    public RawFrameBlock(CharmModule module) {
        super(module, "raw_frame", AbstractBlock.Settings.copy(Blocks.STONE));
    }
}

package svenhjol.strange.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import svenhjol.charm.base.CharmModule;

public class RawFrameBlock extends BaseFrameBlock {
    public RawFrameBlock(CharmModule module) {
        super(module, "raw_frame", AbstractBlock.Settings.copy(Blocks.STONE));
    }
}

package svenhjol.strange.runeportals;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlock;

public class RawFrameBlock extends CharmBlock {
    public RawFrameBlock(CharmModule module) {
        super(module, "raw_frame", AbstractBlock.Settings.copy(Blocks.STONE));
    }
}

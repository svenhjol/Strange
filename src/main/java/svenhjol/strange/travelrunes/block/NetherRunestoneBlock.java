package svenhjol.strange.travelrunes.block;

import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;

public class NetherRunestoneBlock extends BaseRunestoneBlock
{
    public NetherRunestoneBlock(MesonModule module)
    {
        super(module, "nether_runestone", Properties.from(Blocks.NETHER_BRICKS));
    }
}

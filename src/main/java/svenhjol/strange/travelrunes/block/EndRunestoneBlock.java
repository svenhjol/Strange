package svenhjol.strange.travelrunes.block;

import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;

public class EndRunestoneBlock extends BaseRunestoneBlock
{
    public EndRunestoneBlock(MesonModule module)
    {
        super(module, "end_runestone", Properties.from(Blocks.END_STONE));
    }
}

package svenhjol.strange.travelrunes.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;

public class StoneRunestoneBlock extends BaseRunestoneBlock
{
    public StoneRunestoneBlock(MesonModule module)
    {
        super(module, "stone_runestone", Block.Properties.from(Blocks.STONE));
    }
}

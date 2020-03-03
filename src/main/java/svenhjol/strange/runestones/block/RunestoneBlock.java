package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public class RunestoneBlock extends MesonBlock
{
    public RunestoneBlock(MesonModule module, int val, boolean mined)
    {
        super(module, "runestone_" + (mined ? "mined_" : "") + val, Block.Properties.from(Blocks.STONE));
    }
}

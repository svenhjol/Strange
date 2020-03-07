package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;

public class RunestoneBlock extends BaseRunestoneBlock {
    public RunestoneBlock(MesonModule module, int runeValue) {
        super(module, "runestone", runeValue, Block.Properties.from(Blocks.STONE));
    }
}

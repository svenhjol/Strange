package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import svenhjol.meson.MesonModule;

public class RunestoneBlock extends BaseRunestoneBlock {
    public RunestoneBlock(MesonModule module, int runeValue) {
        super(module, "runestone", runeValue, Block.Properties.from(Blocks.STONE));
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        return 1;
    }
}

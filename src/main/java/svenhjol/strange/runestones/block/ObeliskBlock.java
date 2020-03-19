package svenhjol.strange.runestones.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import svenhjol.meson.MesonModule;

public class ObeliskBlock extends BaseRunestoneBlock {
    public ObeliskBlock(MesonModule module, int runeValue) {
        super(module, "obelisk", runeValue, Properties.from(Blocks.STONE));
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        return 1;
    }
}

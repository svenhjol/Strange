package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import svenhjol.meson.MesonModule;
import svenhjol.strange.runestones.tileentity.RunestoneTileEntity;

import javax.annotation.Nullable;

public class RunestoneBlock extends BaseRunestoneBlock {
    public RunestoneBlock(MesonModule module, int runeValue) {
        super(module, "runestone", runeValue, Block.Properties.from(Blocks.STONE));
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        return 1;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RunestoneTileEntity();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        // don't
    }
}

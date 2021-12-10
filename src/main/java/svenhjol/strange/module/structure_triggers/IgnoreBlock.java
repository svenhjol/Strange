package svenhjol.strange.module.structure_triggers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import svenhjol.charm.block.CharmBlock;
import svenhjol.charm.loader.CharmModule;

public class IgnoreBlock extends CharmBlock {
    public static final VoxelShape SHAPE;

    protected IgnoreBlock(CharmModule module) {
        super(module, "ignore_block", Properties.copy(Blocks.GLASS).noOcclusion());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> list) {
        // no
    }

    static {
        SHAPE = Block.box(2, 2, 2, 14, 14, 14);
    }
}

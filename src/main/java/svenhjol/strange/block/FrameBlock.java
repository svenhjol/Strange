package svenhjol.strange.block;

import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import svenhjol.charm.base.CharmModule;

public class FrameBlock extends BaseFrameBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty RUNE = IntProperty.of("rune", 0, 25);

    protected static final VoxelShape EAST_FRAME_SHAPE;
    protected static final VoxelShape EAST_RUNE_SHAPE;
    protected static final VoxelShape WEST_FRAME_SHAPE;
    protected static final VoxelShape WEST_RUNE_SHAPE;
    protected static final VoxelShape NORTH_FRAME_SHAPE;
    protected static final VoxelShape NORTH_RUNE_SHAPE;
    protected static final VoxelShape SOUTH_FRAME_SHAPE;
    protected static final VoxelShape SOUTH_RUNE_SHAPE;
    protected static final VoxelShape EAST_SHAPE;
    protected static final VoxelShape WEST_SHAPE;
    protected static final VoxelShape NORTH_SHAPE;
    protected static final VoxelShape SOUTH_SHAPE;

    public FrameBlock(CharmModule module) {
        super(module, "frame", AbstractBlock.Settings.copy(Blocks.STONE));
        this.setDefaultState(this.getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(RUNE, 0));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(FACING) == Direction.WEST) {
            return WEST_SHAPE;
        } else if (state.get(FACING) == Direction.SOUTH) {
            return SOUTH_SHAPE;
        } else if (state.get(FACING) == Direction.EAST) {
            return EAST_SHAPE;
        } else {
            return NORTH_SHAPE;
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, RUNE);
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state
            .with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    static {
        EAST_FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 15.0D, 16.0D, 16.0D);
        EAST_RUNE_SHAPE = Block.createCuboidShape(15.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);

        WEST_FRAME_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        WEST_RUNE_SHAPE = Block.createCuboidShape(0.0D, 2.0D, 2.0D, 1.0D, 14.0D, 14.0D);

        NORTH_FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 1.0D, 16.0D, 16.0D, 16.0D);
        NORTH_RUNE_SHAPE = Block.createCuboidShape(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 1.0D);

        SOUTH_FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 15.0D);
        SOUTH_RUNE_SHAPE = Block.createCuboidShape(2.0D, 2.0D, 15.0D, 14.0D, 14.0D, 16.0D);

        EAST_SHAPE = VoxelShapes.union(EAST_FRAME_SHAPE, EAST_RUNE_SHAPE);
        WEST_SHAPE = VoxelShapes.union(WEST_FRAME_SHAPE, WEST_RUNE_SHAPE);
        NORTH_SHAPE = VoxelShapes.union(NORTH_FRAME_SHAPE, NORTH_RUNE_SHAPE);
        SOUTH_SHAPE = VoxelShapes.union(SOUTH_FRAME_SHAPE, SOUTH_RUNE_SHAPE);
    }
}

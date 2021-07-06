package svenhjol.strange.module.rune_portals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import svenhjol.charm.block.CharmBlock;
import svenhjol.charm.loader.CharmModule;

import java.util.Random;

public class PortalFrameBlock extends CharmBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty RUNE = IntegerProperty.create("rune", 0, 25);

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

    public PortalFrameBlock(CharmModule module) {
        super(module, "portal_frame", Properties.copy(Blocks.CRYING_OBSIDIAN));
        this.registerDefaultState(this.defaultBlockState()
            .setValue(FACING, Direction.NORTH)
            .setValue(RUNE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(FACING) == Direction.WEST) {
            return WEST_SHAPE;
        } else if (state.getValue(FACING) == Direction.SOUTH) {
            return SOUTH_SHAPE;
        } else if (state.getValue(FACING) == Direction.EAST) {
            return EAST_SHAPE;
        } else {
            return NORTH_SHAPE;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, RUNE);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        // ekki
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return state
            .setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * Copypasta from CryingObdisianBlock
     */
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (random.nextInt(10) == 0) {
            Direction direction = Direction.getRandom(random);
            if (direction != Direction.UP) {
                BlockPos blockPos2 = blockPos.relative(direction);
                BlockState blockState2 = level.getBlockState(blockPos2);
                if (!blockState.canOcclude() || !blockState2.isFaceSturdy(level, blockPos2, direction.getOpposite())) {
                    double d = direction.getStepX() == 0 ? random.nextDouble() : 0.5D + (double)direction.getStepX() * 0.6D;
                    double e = direction.getStepY() == 0 ? random.nextDouble() : 0.5D + (double)direction.getStepY() * 0.6D;
                    double f = direction.getStepZ() == 0 ? random.nextDouble() : 0.5D + (double)direction.getStepZ() * 0.6D;
                    level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double)blockPos.getX() + d, (double)blockPos.getY() + e, (double)blockPos.getZ() + f, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    static {
        EAST_FRAME_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 15.0D, 16.0D, 16.0D);
        EAST_RUNE_SHAPE = Block.box(15.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);

        WEST_FRAME_SHAPE = Block.box(1.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        WEST_RUNE_SHAPE = Block.box(0.0D, 2.0D, 2.0D, 1.0D, 14.0D, 14.0D);

        NORTH_FRAME_SHAPE = Block.box(0.0D, 0.0D, 1.0D, 16.0D, 16.0D, 16.0D);
        NORTH_RUNE_SHAPE = Block.box(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 1.0D);

        SOUTH_FRAME_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 15.0D);
        SOUTH_RUNE_SHAPE = Block.box(2.0D, 2.0D, 15.0D, 14.0D, 14.0D, 16.0D);

        EAST_SHAPE = Shapes.or(EAST_FRAME_SHAPE, EAST_RUNE_SHAPE);
        WEST_SHAPE = Shapes.or(WEST_FRAME_SHAPE, WEST_RUNE_SHAPE);
        NORTH_SHAPE = Shapes.or(NORTH_FRAME_SHAPE, NORTH_RUNE_SHAPE);
        SOUTH_SHAPE = Shapes.or(SOUTH_FRAME_SHAPE, SOUTH_RUNE_SHAPE);
    }
}

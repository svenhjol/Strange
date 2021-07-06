package svenhjol.strange.module.scrollkeepers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
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

import javax.annotation.Nullable;
import java.util.Random;

public class WritingDeskBlock extends CharmBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 3);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape TOP;
    public static final VoxelShape LEGS;
    public static final VoxelShape COLLISION_SHAPE;
    public static final VoxelShape OUTLINE_SHAPE;
    public static final int NUM_VARIANTS = 4;

    public WritingDeskBlock(CharmModule module) {
        super(module, Scrollkeepers.BLOCK_ID.getPath(), Properties.copy(Blocks.CARTOGRAPHY_TABLE));
        this.registerDefaultState(defaultBlockState().setValue(VARIANT, 0));
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        Random r = new Random(pos.asLong());
        world.setBlockAndUpdate(pos, state.setValue(VARIANT, r.nextInt(NUM_VARIANTS)));
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public CreativeModeTab getItemGroup() {
        return CreativeModeTab.TAB_DECORATIONS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }

    static {
        TOP = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 13.0D, 16.0D);
        LEGS = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);
        COLLISION_SHAPE = Shapes.or(TOP, LEGS);
        OUTLINE_SHAPE = Shapes.or(TOP, LEGS);
    }
}

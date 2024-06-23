package svenhjol.strange.feature.runestones.common;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import svenhjol.charm.charmony.feature.FeatureResolver;
import svenhjol.strange.feature.runestones.Runestones;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class RunestoneBlock extends BaseEntityBlock implements FeatureResolver<Runestones>, SimpleWaterloggedBlock {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    public static final MapCodec<RunestoneBlock> CODEC = simpleCodec(RunestoneBlock::new);
    public static final VoxelShape B1, B2, B3;
    public static final VoxelShape ACTIVATED_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public RunestoneBlock() {
        this(Properties.ofFullCopy(Blocks.STONE)
            .pushReaction(PushReaction.IGNORE)
            .lightLevel(state -> state.getValue(ACTIVATED) ? 12 : 0));
    }

    protected RunestoneBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(getStateDefinition().any()
            .setValue(ACTIVATED, false)
            .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        feature().handlers.prepareRunestone(level, pos);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        if (level.isClientSide()) return null;
        return RunestoneBlock.createTickerHelper(blockEntity,
            feature().registers.blockEntity.get(),
            RunestoneBlockEntity::serverTick);
    }

    @Override
    public Class<Runestones> typeForFeature() {
        return Runestones.class;
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (state.getValue(ACTIVATED)) {
            return ACTIVATED_SHAPE;
        }
        return super.getShape(state, getter, pos, context);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (state.getValue(ACTIVATED)) {
            return ACTIVATED_SHAPE;
        }
        return super.getCollisionShape(state, getter, pos, context);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level levelAccessor = blockPlaceContext.getLevel();
        boolean bl = levelAccessor.getFluidState(blockPlaceContext.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, bl);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED)) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (!state.getValue(ACTIVATED)) {
            return;
        }

        if (random.nextFloat() > 0.8f) {
            return;
        }

        var particle = ParticleTypes.ENCHANT;
        var dist = 3.0d;

        for (var i = 0; i < 4; i++) {
            level.addParticle(particle, pos.getX() + 0.5d, pos.getY() + 1.3d, pos.getZ() + 0.5d,
                (dist / 2) - (random.nextDouble() * dist), random.nextDouble() - 1.65d, (dist / 2) - (random.nextDouble() * dist));
        }
    }

    public static class BlockItem extends net.minecraft.world.item.BlockItem {
        public BlockItem(Supplier<RunestoneBlock> block) {
            super(block.get(), new Properties());
        }
    }

    static {
        B1 = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 3.0d, 16.0d);
        B2 = Block.box(4.0d, 3.0d, 4.0d, 12.0d, 13.0d, 12.0d);
        B3 = Block.box(0.0d, 13.0d, 0.0d, 16.0d, 16.0d, 16.0d);
        ACTIVATED_SHAPE = Shapes.or(B1, B2, B3);
    }
}

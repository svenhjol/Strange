package svenhjol.strange.feature.runestones.common;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import svenhjol.charm.charmony.feature.FeatureResolver;
import svenhjol.strange.feature.runestones.Runestones;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class RunestoneBlock extends BaseEntityBlock implements FeatureResolver<Runestones> {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    public static final MapCodec<RunestoneBlock> CODEC = simpleCodec(RunestoneBlock::new);

    public RunestoneBlock() {
        this(Properties.ofFullCopy(Blocks.STONE)
            .pushReaction(PushReaction.DESTROY)
            .lightLevel(state -> state.getValue(ACTIVATED) ? 12 : 0));
    }

    protected RunestoneBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(getStateDefinition().any()
            .setValue(ACTIVATED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
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
}

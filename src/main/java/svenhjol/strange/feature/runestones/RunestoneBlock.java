package svenhjol.strange.feature.runestones;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import svenhjol.charmony.base.CharmonyBlockItem;
import svenhjol.charmony.base.CharmonyBlockWithEntity;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;

import java.util.function.Supplier;

public class RunestoneBlock extends CharmonyBlockWithEntity {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    static final MapCodec<RunestoneBlock> CODEC = simpleCodec(RunestoneBlock::new);

    public RunestoneBlock() {
        this(Properties.ofFullCopy(Blocks.STONE)
            .pushReaction(PushReaction.DESTROY)
            .lightLevel(state -> state.getValue(ACTIVATED) ? 12 : 0));
    }

    private RunestoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(
            getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVATED, false)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        tryDropActivatedItem(level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var held = player.getItemInHand(hand);
        var log = Mods.common(Strange.ID).log();

        if (level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone && state.getBlock() instanceof RunestoneBlock block) {
            if (Runestones.BLOCK_DEFINITIONS.containsKey(block)) {
                var definition = Runestones.BLOCK_DEFINITIONS.get(block);
                var activationItem = (Item)definition.activationItem().get();

                if (!isValid(runestone)) {
                    return explode(level, pos);
                }

                if (state.getValue(ACTIVATED)) {
                    if (player instanceof ServerPlayer serverPlayer && !Runestones.tryTeleport(serverPlayer, runestone)) {
                        return explode(level, pos);
                    }

                    return InteractionResult.sidedSuccess(level.isClientSide);
                }

                if (!held.is(activationItem)) {
                    return InteractionResult.PASS;
                }

                if (runestone.type == null) {
                    log.warn(getClass(), "Runestone has no type at pos " + pos);
                    return explode(level, pos);
                }

                if (runestone.destination == null) {
                    log.warn(getClass(), "Runestone has no destination at pos " + pos);
                    return explode(level, pos);
                }

                state = state.setValue(ACTIVATED, true);
                level.setBlockAndUpdate(pos, state);
                level.playSound(null, pos, Runestones.activateSound.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }

                if (level instanceof ServerLevel serverLevel && !Runestones.tryLocate(serverLevel, runestone)) {
                    return explode(level, pos);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);

            } else {
                return explode(level, pos);
            }
        }

        return super.use(state, level, pos, player, hand, hitResult);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, entity, itemStack);
        Runestones.prepareRunestone(level, pos);
    }

    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (!state.getValue(ACTIVATED)) {
            return;
        }

        if (random.nextFloat() > 0.9f) {
            return;
        }

        var particle = ParticleTypes.PORTAL;
        var dist = 3.0d;

        for (var i = 0; i < 3; i++) {
            level.addParticle(particle, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d,
                (dist / 2) - (random.nextDouble() * dist), random.nextDouble() - 0.25d, (dist / 2) - (random.nextDouble() * dist));
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVATED);
    }

    private InteractionResult explode(Level level, BlockPos pos) {
        tryDropActivatedItem(level, pos);
        level.explode(null, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 1, Level.ExplosionInteraction.BLOCK);
        level.removeBlock(pos, false);
        return InteractionResult.FAIL;
    }

    private void tryDropActivatedItem(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);

        // Try drop activated item
        if (state.getBlock() instanceof RunestoneBlock block && state.getValue(RunestoneBlock.ACTIVATED)) {
            var definition = Runestones.BLOCK_DEFINITIONS.get(block);
            if (definition != null) {
                var stack = new ItemStack(definition.activationItem().get());
                var itemEntity = new ItemEntity(level, pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d, stack);
                level.addFreshEntity(itemEntity);
            }
        }
    }

    private boolean isValid(RunestoneBlockEntity runestone) {
        var log = Mods.common(Strange.ID).log();
        var pos = runestone.getBlockPos();

        if (runestone.type == null) {
            log.warn(getClass(), "Runestone has no type at pos " + pos);
            return false;
        }

        if (runestone.destination == null) {
            log.warn(getClass(), "Runestone has no destination at pos " + pos);
            return false;
        }

        return true;
    }

    static final class BlockItem extends CharmonyBlockItem {
        public <T extends RunestoneBlock> BlockItem(Supplier<T> block) {
            super(block, new Item.Properties());
        }
    }
}

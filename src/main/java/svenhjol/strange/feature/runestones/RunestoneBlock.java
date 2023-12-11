package svenhjol.strange.feature.runestones;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import svenhjol.charmony.base.CharmonyBlockItem;
import svenhjol.charmony.base.CharmonyBlockWithEntity;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.TagHelper;
import svenhjol.strange.Strange;

import java.util.function.Supplier;

public class RunestoneBlock extends CharmonyBlockWithEntity {
    static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    static final MapCodec<RunestoneBlock> CODEC = simpleCodec(RunestoneBlock::new);

    public RunestoneBlock() {
        this(Properties.ofFullCopy(Blocks.STONE)
            .pushReaction(PushReaction.DESTROY)
            .lightLevel(state -> state.getValue(ACTIVATED) ? 8 : 0));
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
                    if (player instanceof ServerPlayer serverPlayer && !tryTeleport(serverPlayer, runestone)) {
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

                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }

                if (level instanceof ServerLevel serverLevel && !tryLocate(serverLevel, runestone)) {
                    return explode(level, pos);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);

            } else {
                return explode(level, pos);
            }
        }

        return super.use(state, level, pos, player, hand, hitResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack itemStack) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone) {
            var log = Mods.common(Strange.ID).log();

            if (state.getBlock() instanceof RunestoneBlock block) {
                if (Runestones.BLOCK_DEFINITIONS.containsKey(block)) {
                    var definition = Runestones.BLOCK_DEFINITIONS.get(block);
                    var opt = definition.getDestination().apply(level, pos);
                    var registryAccess = level.registryAccess();
                    var random = level.getRandom();

                    if (opt.isPresent()) {
                        var tag = opt.get();
                        if (tag.registry() == Registries.BIOME) {
                            var biomeTag = (TagKey<Biome>) tag;
                            var biomeRegistry = registryAccess.registryOrThrow(biomeTag.registry());
                            var destinations = TagHelper.getValues(biomeRegistry, biomeTag)
                                .stream().map(biomeRegistry::getKey).toList();

                            if (destinations.isEmpty()) {
                                log.warn(getClass(), "Empty biome destinations for runestone at pos " + pos);
                                return;
                            }

                            runestone.destination = destinations.get(random.nextInt(destinations.size()));
                            runestone.type = DestinationType.BIOME;
                            log.debug(getClass(), "Set biome " + runestone.destination + " for runestone at pos " + pos);

                        } else if (tag.registry() == Registries.STRUCTURE) {
                            var structureTag = (TagKey<Structure>) tag;
                            var structureRegistry = registryAccess.registryOrThrow(structureTag.registry());
                            var destinations = TagHelper.getValues(structureRegistry, structureTag)
                                .stream().map(structureRegistry::getKey).toList();

                            if (destinations.isEmpty()) {
                                log.warn(getClass(), "Empty structure destinations for runestone at pos " + pos);
                                return;
                            }

                            runestone.destination = destinations.get(random.nextInt(destinations.size()));
                            runestone.type = DestinationType.STRUCTURE;
                            log.debug(getClass(), "Set structure " + runestone.destination + " for runestone at pos " + pos);

                        }
                    } else {
                        log.warn(getClass(), "Failed to run getDestination on runestone at " + pos);
                    }
                } else {
                    log.warn(getClass(), "No definition found for runestone block " + block + " at pos " + pos);
                }
            }
        }

        super.setPlacedBy(level, pos, state, entity, itemStack);
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

        for (var i = 0; i < 2; i++) {
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

    private boolean tryLocate(ServerLevel level, RunestoneBlockEntity runestone) {
        var log = Mods.common(Strange.ID).log();
        var pos = runestone.getBlockPos();
        var random = RandomSource.create(pos.asLong());
        var target = RunestoneHelper.addRandomOffset(level, pos, random, 1000, 2000);
        var registryAccess = level.registryAccess();

        switch (runestone.type) {
            case BIOME -> {
                var result = level.findClosestBiome3d(x -> x.is(runestone.destination), target, 6400, 32, 64);
                if (result == null) {
                    log.warn(getClass(), "Could not locate biome for " + runestone.destination);
                    return false;
                }

                runestone.target = result.getFirst();
            }
            case STRUCTURE -> {
                var structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);
                var structure = structureRegistry.get(runestone.destination);
                if (structure == null) {
                    log.warn(getClass(), "Could not get registered structure for " + runestone.destination);
                    return false;
                }

                // Wrap structure in holder and holderset so that it's in the right format for find
                var set = HolderSet.direct(Holder.direct(structure));
                var result = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, set, target, 100, false);

                if (result == null) {
                    log.warn(getClass(), "Could not locate structure for " + runestone.destination);
                    return false;
                }

                runestone.target = result.getFirst();
            }
            default -> {
                log.warn(getClass(), "Not a valid destination type for runestone at " + pos);
                return false;
            }
        }

        runestone.setChanged();
        return true;
    }

    private boolean tryTeleport(ServerPlayer player, RunestoneBlockEntity runestone) {
        runestone.discovered = player.getScoreboardName();
        runestone.setChanged();

        var teleport = new RunestoneTeleport(player, runestone.target);
        teleport.teleport();
        Runestones.TELEPORTS.put(player.getUUID(), teleport);

        return true;
    }

    static final class BlockItem extends CharmonyBlockItem {
        public <T extends RunestoneBlock> BlockItem(Supplier<T> block) {
            super(block, new Item.Properties());
        }
    }
}

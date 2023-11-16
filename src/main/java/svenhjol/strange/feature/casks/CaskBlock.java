package svenhjol.strange.feature.casks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import svenhjol.charmony.base.CharmonyBlockItem;
import svenhjol.charmony.base.CharmonyBlockWithEntity;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.feature.advancements.Advancements;
import svenhjol.charmony.iface.IFuelProvider;
import svenhjol.strange.Strange;

import java.util.Objects;
import java.util.function.Supplier;

public class CaskBlock extends CharmonyBlockWithEntity implements IFuelProvider {
    static final DirectionProperty FACING = BlockStateProperties.FACING;
    static final VoxelShape X1, X2, X3, X4;
    static final VoxelShape Y1, Y2, Y3, Y4;
    static final VoxelShape Z1, Z2, Z3, Z4;
    static final VoxelShape X_SHAPE;
    static final VoxelShape Y_SHAPE;
    static final VoxelShape Z_SHAPE;
    static final int FUEL_TIME = 300;

    public CaskBlock(CommonFeature feature) {
        super(feature, Properties.of()
            .strength(2.5f)
            .sound(SoundType.WOOD));

        registerDefaultState(defaultBlockState()
            .setValue(FACING, Direction.NORTH));
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var held = player.getItemInHand(hand);
        var blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof CaskBlockEntity cask) {
            if (!level.isClientSide) {
                if (held.getItem() == Items.NAME_TAG && held.hasCustomHoverName()) {

                    // Name the cask using a name tag.
                    cask.name = held.getHoverName().getContents().toString();
                    cask.setChanged();

                    // TODO: custom sound
                    level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.85F, 1.1F);
                    held.shrink(1);

                } else if (held.getItem() == Items.GLASS_BOTTLE) {

                    // Take a bottle of liquid from the cask using a glass bottle.
                    var out = cask.take(held);
                    if (out != null) {
                        player.getInventory().add(out);

                        if (cask.portions > 0) {
                            // TODO: custom sounds
                            level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.6F, 1.0F);
                            level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.7F, 1.0F);
                        } else {
                            level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.5F, 1.0F);
                        }

                        if (cask.portions > 1 && cask.effects.size() > 1) {
                            triggerTookLiquidFromCask(player);
                        }
                    }

                } else if (held.getItem() == Items.POTION) {

                    // Add a bottle of liquid to the cask using a filled glass bottle.
                    var result = cask.add(held);
                    if (result) {
                        // TODO: custom sounds
                        level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.6F, 1.0F);
                        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.9F, 0.9F);

                        // give the glass bottle back to the player
                        player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));

                        // Let nearby players know an item was added to the cask
                        CasksNetwork.AddedToCask.send(level, pos);

                        // do advancement for filling with potions
                        if (cask.portions > 1 && cask.effects.size() > 1) {
                            triggerAddedLiquidToCask(player);
                        }
                    }
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (level.getBlockEntity(pos) instanceof CaskBlockEntity cask) {
            if (itemStack.hasCustomHoverName()) {
                cask.name = itemStack.getHoverName().getContents().toString();
            }

            // Try restore contents from tag
            var tag = itemStack.getTag();
            if (tag != null && tag.contains(Casks.STORED_POTIONS_TAG)) {
                cask.load(tag.getCompound(Casks.STORED_POTIONS_TAG));
            }

            cask.setChanged();
        }

        super.setPlacedBy(level, pos, state, placer, itemStack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CaskBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public int fuelTime() {
        return FUEL_TIME;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CaskBlockEntity cask && cask.portions > 0) {
            return Math.round((cask.portions / (float) Casks.maxPortions) * 16);
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch ((state.getValue(CaskBlock.FACING)).getAxis()) {
            default -> X_SHAPE;
            case Z -> Z_SHAPE;
            case Y -> Y_SHAPE;
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch ((state.getValue(CaskBlock.FACING)).getAxis()) {
            default -> X_SHAPE;
            case Z -> Z_SHAPE;
            case Y -> Y_SHAPE;
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(1) == 0) {
            var blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof CaskBlockEntity cask && cask.portions > 0) {
                var effects = cask.effects
                    .stream()
                    .map(BuiltInRegistries.MOB_EFFECT::get)
                    .filter(Objects::nonNull)
                    .toList();

                if (effects.isEmpty()) {
                    // There's only water in the cask.
                    createWaterParticle(level, pos);
                } else {
                    // Create particles for each effect color
                    effects.forEach(effect -> createEffectParticle(level, pos, effect));
                }
            }
        }
    }

    void createWaterParticle(Level level, BlockPos pos) {
        var random = level.getRandom();

        level.addParticle(ParticleTypes.DRIPPING_WATER,
            pos.getX() + random.nextDouble(),
            pos.getY() + 0.7d,
            pos.getZ() + random.nextDouble(),
            0.0d, 0.0d, 0.0d);
    }

    void createEffectParticle(Level level, BlockPos pos, MobEffect effect) {
        var random = level.getRandom();
        var color = effect.getColor();

        var r = (color >> 16 & 255) / 255.0D;
        var g = (color >> 8 & 255) / 255.0D;
        var b = (color & 255) / 255.0D;

        level.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT,
            pos.getX() + 0.13d + (0.7d * random.nextDouble()),
            pos.getY() + 0.5d,
            pos.getZ() + 0.13d + (0.7d * random.nextDouble()),
            r, g, b);
    }

    public static void triggerAddedLiquidToCask(Player player) {
        Advancements.trigger(new ResourceLocation(Strange.ID, "added_liquid_to_cask"), player);
    }

    public static void triggerTookLiquidFromCask(Player player) {
        Advancements.trigger(new ResourceLocation(Strange.ID, "took_liquid_from_cask"), player);
    }

    static {
        X1 = Block.box(1.0D, 0.0D, 4.0D, 15.0D, 16.0D, 12.0D);
        X2 = Block.box(1.0D, 1.0D, 2.0D, 15.0D, 15.0D, 14.0D);
        X3 = Block.box(1.0D, 2.0D, 1.0D, 15.0D, 14.0D, 15.0D);
        X4 = Block.box(1.0D, 4.0D, 0.0D, 15.0D, 12.0D, 16.0D);
        Y1 = Block.box(4.0D, 1.0D, 0.0D, 12.0D, 15.0D, 16.0D);
        Y2 = Block.box(2.0D, 1.0D, 1.0D, 14.0D, 15.0D, 15.0D);
        Y3 = Block.box(1.0D, 1.0D, 2.0D, 15.0D, 15.0D, 14.0D);
        Y4 = Block.box(0.0D, 1.0D, 4.0D, 16.0D, 15.0D, 12.0D);
        Z1 = Block.box(4.0D, 0.0D, 1.0D, 12.0D, 16.0D, 15.0D);
        Z2 = Block.box(2.0D, 1.0D, 1.0D, 14.0D, 15.0D, 15.0D);
        Z3 = Block.box(1.0D, 2.0D, 1.0D, 15.0D, 14.0D, 15.0D);
        Z4 = Block.box(0.0D, 4.0D, 1.0D, 16.0D, 12.0D, 15.0D);
        X_SHAPE = Shapes.or(X1, X2, X3, X4);
        Y_SHAPE = Shapes.or(Y1, Y2, Y3, Y4);
        Z_SHAPE = Shapes.or(Z1, Z2, Z3, Z4);
    }

    static class BlockItem extends CharmonyBlockItem {
        public BlockItem(Supplier<CaskBlock> block) {
            super(block, new Properties());
        }
    }
}

package svenhjol.strange.feature.cooking_pots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import svenhjol.charmony.base.CharmonyBlockItem;
import svenhjol.charmony.base.CharmonyBlockWithEntity;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.feature.cooking_pots.CookingPotContainers.EmptyContainer;
import svenhjol.strange.feature.cooking_pots.CookingPotContainers.InputContainer;

public class CookingPotBlock extends CharmonyBlockWithEntity implements WorldlyContainerHolder {
    static IntegerProperty PORTIONS = IntegerProperty.create("portions", 0, CookingPots.getMaxPortions());
    static EnumProperty<CookingStatus> COOKING_STATUS = EnumProperty.create("cooking_status", CookingStatus.class);
    static final VoxelShape RAY_TRACE_SHAPE;
    static final VoxelShape OUTLINE_SHAPE;

    public CookingPotBlock(CommonFeature feature) {
        super(feature, Properties.of()
            .requiresCorrectToolForDrops()
            .strength(2.0f)
            .mapColor(MapColor.COLOR_ORANGE)
            .sound(SoundType.COPPER)
            .noOcclusion());

        registerDefaultState(defaultBlockState()
            .setValue(PORTIONS, 0)
            .setValue(COOKING_STATUS, CookingStatus.NONE));
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var held = player.getItemInHand(hand);
        var blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof CookingPotBlockEntity pot) {
            if (held.is(Items.WATER_BUCKET) && pot.canAddWater()) {

                // Add a water bucket to the pot.
                state = state.setValue(PORTIONS, CookingPots.getMaxPortions());
                level.setBlock(pos, state, 2);
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);

                if (!player.getAbilities().instabuild) {
                    player.getInventory().add(new ItemStack(Items.BUCKET));
                    held.shrink(1);
                }

            } else if (held.is(Items.POTION)
                && PotionUtils.getPotion(held) == Potions.WATER
                && pot.canAddWater()) {

                // Add a bottle of water. Increase portions by 1.
                state = state.setValue(PORTIONS, state.getValue(PORTIONS) + 1);
                level.setBlock(pos, state, 2);

                if (!player.getAbilities().instabuild) {
                    player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                    held.shrink(1);
                }

            } else if (held.isEdible()) {

                // Add a food item to the pot.
                var result = pot.add(held);
                if (result) {

                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }

            } else if (held.is(Items.BOWL)) {

                // Take a bowl of stew from the pot using a wooden bowl.
                var out = pot.take();
                if (!out.isEmpty()) {
                    player.getInventory().add(out);
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CookingPotBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return RAY_TRACE_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PORTIONS, COOKING_STATUS);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        int portions = state.getValue(PORTIONS);

        if (!(level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot)) {
            return 0;
        }

        if (!pot.hasFinishedCooking()) {
            return 0;
        }

        return portions;
    }

    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot && pot.canAddFood()) {
            return new InputContainer(level, pos, state);
        }
        return new EmptyContainer();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot) {
            if (pot.hasFire() && !pot.isEmpty()) {
                int portions = state.getValue(PORTIONS);

                if (random.nextInt(1) == 0) {
                    level.addParticle(ParticleTypes.SMOKE,
                        pos.getX() + 0.13d + (0.7d * random.nextDouble()),
                        pos.getY() + portions * 0.153d,
                        pos.getZ() + 0.13d + (0.7d * random.nextDouble()),
                        0.0d, 0.0d, 0.0d);
                }

                if (random.nextInt(16) == 0) {
                    level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                        CookingPots.ambientSound.get(),
                        SoundSource.BLOCKS,
                        0.15f + (0.15f * random.nextFloat()),
                        random.nextFloat() * 0.7f + 0.6f, false);
                }
            }

        }
    }

    static class BlockItem extends CharmonyBlockItem {
        public BlockItem() {
            super(CookingPots.block, new Properties());
        }
    }

    static {
        RAY_TRACE_SHAPE = box(2.0d, 4.0d, 2.0d, 14.0d, 16.0d, 14.0d);
        OUTLINE_SHAPE = Shapes.join(Shapes.block(),
            Shapes.or(
                box(0.0d, 0.0d, 4.0d, 16.0d, 3.0d, 12.0d),
                box(4.0d, 0.0d, 0.0d, 12.0d, 3.0d, 16.0d),
                box(2.0d, 0.0d, 2.0d, 14.0d, 3.0d, 14.0d), RAY_TRACE_SHAPE),
            BooleanOp.ONLY_FIRST);
    }
}

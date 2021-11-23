package svenhjol.strange.module.rubble;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.loader.CharmModule;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RubbleBlock extends CharmBlockWithEntity {
    public static IntegerProperty LAYER;
    public static final List<VoxelShape> SHAPES = new ArrayList<>();

    public RubbleBlock(CharmModule module) {
        super(module, "rubble", FabricBlockSettings.of(Material.SAND, MaterialColor.COLOR_GRAY)
            .strength(8.0F)
            .requiresTool()
            .sound(SoundType.GRAVEL));

        registerDefaultState(defaultBlockState().setValue(LAYER, 0));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RubbleBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYER);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int level = state.getValue(LAYER);
        return SHAPES.get(level);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int level = state.getValue(LAYER);
        return SHAPES.get(level);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int layer = state.getValue(LAYER);
        Random random = level.random;
        ItemStack held = player.getItemInHand(hand);

        if (layer <= 8) {
            layerEffect(level, pos, layer);
            level.playSound(player, pos, SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

            if (!level.isClientSide) {
                ServerLevel serverLevel = (ServerLevel) level;

                // get the block entity
                RubbleBlockEntity rubble = getBlockEntity(serverLevel, pos);
                if (rubble == null) {
                    return fail(serverLevel, pos);
                }

                // populate for first time
                if (layer == 0 && !populate((ServerLevel) level, pos, player)) {
                    return fail(serverLevel, pos);
                }

                if (layer == 8) {
                    return success(serverLevel, pos, player);
                }

                // test to see if it can be levelled
                long levelTicks = rubble.getLayerTicks();
                boolean shovel = held.getItem() instanceof ShovelItem;
                boolean waited = levelTicks > 0 && serverLevel.getGameTime() - levelTicks > 200;

                if (waited || !shovel) {
                    return fail(serverLevel, pos);
                }
                rubble.setLayerTicks(level.getGameTime());
                rubble.setChanged();
                WorldHelper.syncBlockEntityToClient((ServerLevel)level, pos);

                if (random.nextFloat() < 0.8F) return InteractionResult.PASS;
                return layer(serverLevel, pos, state, layer);
            }

            return InteractionResult.sidedSuccess(true);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide) return;

        fail((ServerLevel)level, pos);
    }

    @Nullable
    public RubbleBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof RubbleBlockEntity)) return null;

        return (RubbleBlockEntity) blockEntity;
    }

    private boolean populate(ServerLevel level, BlockPos pos, LivingEntity entity) {
        Random random = level.random;

        RubbleBlockEntity rubble = getBlockEntity(level, pos);
        if (rubble == null) return false;

        LootTable lootTable = level.getServer().getLootTables().get(Rubble.LOOT);
        List<ItemStack> list = lootTable.getRandomItems((new LootContext.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .withRandom(random)
            .create(LootContextParamSets.CHEST)));

        if (list.isEmpty()) return false;

        ItemStack stack;
        if (list.size() == 1 || random.nextFloat() < 0.25F) {
            // fetch from the default loot table
            stack = list.get(0);
        } else {
            // fetch from additional loot tables (legendary items, etc)
            stack = list.get(random.nextInt(list.size() - 1) + 1);
        }

        rubble.setItem(stack);
        rubble.setChanged();
        return true;
    }

    private InteractionResult layer(ServerLevel level, BlockPos pos, BlockState state, int layer) {
        level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.setBlock(pos, state.setValue(LAYER, layer + 1), 2);

        RubbleBlockEntity rubble = getBlockEntity(level, pos);
        if (rubble == null) return InteractionResult.FAIL;

        rubble.setChanged();
        return InteractionResult.SUCCESS;
    }

    private InteractionResult fail(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 0.9F, 0.9F);
        level.playSound(null, pos, SoundEvents.CORAL_BLOCK_BREAK, SoundSource.BLOCKS, 0.75F, 1.17F);

        level.removeBlockEntity(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);

        return InteractionResult.FAIL;
    }

    private InteractionResult success(ServerLevel level, BlockPos pos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return InteractionResult.FAIL;

        RubbleBlockEntity rubble = (RubbleBlockEntity)blockEntity;
        ItemStack itemStack = rubble.getItem();

        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 0.9F);
        dropItem(level, pos, itemStack);
        Rubble.triggerHarvestedRubble((ServerPlayer)player);

        level.removeBlockEntity(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);

        return InteractionResult.sidedSuccess(true);
    }

    private void layerEffect(Level level, BlockPos pos, int layer) {
        if (!level.isClientSide) return;
        double y = 1.29D - ((double)layer / 8);

        for (int i = 0; i < 12; i++) {
            level.addParticle(ParticleTypes.ASH,
                (double) pos.getX() + level.random.nextDouble(),
                (double) pos.getY() + y,
                (double) pos.getZ() + level.random.nextDouble(),
                0.0D, 0.0D, 0.0D);
        }
    }

    private void dropItem(ServerLevel world, BlockPos pos, ItemStack stack) {
        world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 0.8F);
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, stack);
        world.addFreshEntity(entity);
    }

    static {
        LAYER = BlockStateProperties.LEVEL_COMPOSTER;
        SHAPES.add(Block.box(0, 0, 0, 16, 16, 16));
        SHAPES.add(Block.box(0, 0, 0, 16, 14, 16));
        SHAPES.add(Block.box(0, 0, 0, 16, 12, 16));
        SHAPES.add(Block.box(0, 0, 0, 16, 10, 16));
        SHAPES.add(Block.box(0, 0, 0, 16, 8, 16));
        SHAPES.add(Block.box(0, 0, 0, 16, 7, 16));
        SHAPES.add(Block.box(0, 0, 0, 16, 6, 16));
        SHAPES.add(Block.box(0, 0, 0, 16, 5, 16));
        SHAPES.add(Block.box(0, 0, 0, 16, 4, 16));
    }
}

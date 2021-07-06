package svenhjol.strange.module.rubble;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
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
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.init.StrangeLoot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RubbleBlock extends CharmBlockWithEntity {
    public static IntegerProperty LEVEL;
    public static final List<VoxelShape> SHAPES = new ArrayList<>();

    public RubbleBlock(CharmModule module) {
        super(module, "rubble", FabricBlockSettings.of(Material.SAND, MaterialColor.COLOR_GRAY)
            .strength(8.0F)
            .requiresTool()
            .breakByTool(FabricToolTags.SHOVELS)
            .sound(SoundType.GRAVEL));

        registerDefaultState(defaultBlockState().setValue(LEVEL, 0));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RubbleBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int level = state.getValue(LEVEL);
        return SHAPES.get(level);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int level = state.getValue(LEVEL);
        return SHAPES.get(level);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int level = state.getValue(LEVEL);
        Random random = world.random;
        ItemStack held = player.getItemInHand(hand);

        if (level <= 8) {
            levelEffect(world, pos, level);
            world.playSound(player, pos, SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

            if (!world.isClientSide) {
                ServerLevel serverWorld = (ServerLevel) world;

                // get the block entity
                RubbleBlockEntity rubble = getBlockEntity(serverWorld, pos);
                if (rubble == null)
                    return fail(serverWorld, pos);

                // populate for first time
                if (level == 0 && !populate((ServerLevel) world, pos, player))
                    return fail(serverWorld, pos);

                if (level == 8)
                    return success(serverWorld, pos, player);

                // test to see if it can be levelled
                long levelTicks = rubble.getLevelTicks();
                boolean shovel = held.getItem() instanceof ShovelItem;
                boolean waited = levelTicks > 0 && serverWorld.getGameTime() - levelTicks > 10;

                if (waited || !shovel)
                    return fail(serverWorld, pos);

                rubble.setLevelTicks(world.getGameTime());

                if (random.nextFloat() < 0.9F)
                    return InteractionResult.PASS;

                return level(serverWorld, pos, state, level);
            }

            return InteractionResult.sidedSuccess(true);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (world.isClientSide)
            return;

        fail((ServerLevel)world, pos);
    }

    @Nullable
    public RubbleBlockEntity getBlockEntity(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof RubbleBlockEntity))
            return null;

        return (RubbleBlockEntity) blockEntity;
    }

    private boolean populate(ServerLevel world, BlockPos pos, LivingEntity entity) {
        Random random = world.random;

        RubbleBlockEntity rubble = getBlockEntity(world, pos);
        if (rubble == null)
            return false;

        LootTable lootTable = world.getServer().getLootTables().get(StrangeLoot.RUBBLE);
        List<ItemStack> list = lootTable.getRandomItems((new LootContext.Builder(world)
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .withRandom(random)
            .create(LootContextParamSets.CHEST)));

        if (list.isEmpty())
            return false;

        ItemStack stack;
        if (list.size() == 1 || random.nextFloat() < 0.44F) {
            // fetch from the default loot table
            stack = list.get(0);
        } else {
            // fetch from additional loot tables (legendary items, etc)
            stack = list.get(random.nextInt(list.size() - 1) + 1);
        }

        rubble.setItemStack(stack);
        rubble.setChanged();
        rubble.sync();

        return true;
    }

    private InteractionResult level(ServerLevel world, BlockPos pos, BlockState state, int level) {
        world.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.setBlock(pos, state.setValue(LEVEL, level + 1), 2);

        RubbleBlockEntity rubble = getBlockEntity(world, pos);
        if (rubble == null)
            return InteractionResult.FAIL;

        rubble.setChanged();
        rubble.sync();

        return InteractionResult.SUCCESS;
    }

    private InteractionResult fail(ServerLevel world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 0.9F, 0.9F);
        world.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.65F, 1.17F);

        dropItem(world, pos, new ItemStack(Blocks.GRAVEL));
        world.removeBlockEntity(pos);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);

        return InteractionResult.FAIL;
    }

    private InteractionResult success(ServerLevel world, BlockPos pos, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return InteractionResult.FAIL;

        RubbleBlockEntity rubble = (RubbleBlockEntity)blockEntity;
        ItemStack itemStack = rubble.getItemStack();

        world.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 0.9F);
        dropItem(world, pos, itemStack);
        Rubble.triggerHarvestedRubble((ServerPlayer)player);

        world.removeBlockEntity(pos);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);

        return InteractionResult.sidedSuccess(true);
    }

    private void levelEffect(Level world, BlockPos pos, int level) {
        if (!world.isClientSide)
            return;

        double y = 1.29D - ((double)level / 8);

        for (int i = 0; i < 12; i++) {
            world.addParticle(ParticleTypes.ASH,
                (double) pos.getX() + world.random.nextDouble(),
                (double) pos.getY() + y,
                (double) pos.getZ() + world.random.nextDouble(),
                0.0D, 0.0D, 0.0D);
        }
    }

    private void dropItem(ServerLevel world, BlockPos pos, ItemStack stack) {
        world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 0.8F);
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, stack);
        world.addFreshEntity(entity);
    }

    static {
        LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
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

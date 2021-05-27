package svenhjol.strange.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlockWithEntity;
import svenhjol.strange.StrangeLoot;
import svenhjol.strange.module.Rubble;
import svenhjol.strange.block.entity.RubbleBlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RubbleBlock extends CharmBlockWithEntity {
    public static IntProperty LEVEL;
    public static final List<VoxelShape> SHAPES = new ArrayList<>();

    public RubbleBlock(CharmModule module) {
        super(module, "rubble", FabricBlockSettings.of(Material.AGGREGATE, MapColor.GRAY)
            .strength(8.0F)
            .requiresTool()
            .breakByTool(FabricToolTags.SHOVELS)
            .sounds(BlockSoundGroup.GRAVEL));

        setDefaultState(getDefaultState().with(LEVEL, 0));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RubbleBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LEVEL);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int level = state.get(LEVEL);
        return SHAPES.get(level);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int level = state.get(LEVEL);
        return SHAPES.get(level);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int level = state.get(LEVEL);
        Random random = world.random;
        ItemStack held = player.getStackInHand(hand);

        if (level <= 8) {
            levelEffect(world, pos, level);
            world.playSound(player, pos, SoundEvents.BLOCK_COMPOSTER_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;

                // get the block entity
                RubbleBlockEntity rubble = getBlockEntity(serverWorld, pos);
                if (rubble == null)
                    return fail(serverWorld, pos);

                // populate for first time
                if (level == 0 && !populate((ServerWorld) world, pos, player))
                    return fail(serverWorld, pos);

                if (level == 8)
                    return success(serverWorld, pos, player);

                // test to see if it can be levelled
                long levelTicks = rubble.getLevelTicks();
                boolean shovel = held.getItem() instanceof ShovelItem;
                boolean waited = levelTicks > 0 && serverWorld.getTime() - levelTicks > 10;

                if (waited || !shovel)
                    return fail(serverWorld, pos);

                rubble.setLevelTicks(world.getTime());

                if (random.nextFloat() < 0.9F)
                    return ActionResult.PASS;

                return level(serverWorld, pos, state, level);
            }

            return ActionResult.success(true);
        }

        return ActionResult.PASS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.isClient)
            return;

        fail((ServerWorld)world, pos);
    }

    @Nullable
    public RubbleBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof RubbleBlockEntity))
            return null;

        return (RubbleBlockEntity) blockEntity;
    }

    private boolean populate(ServerWorld world, BlockPos pos, LivingEntity entity) {
        Random random = world.random;

        RubbleBlockEntity rubble = getBlockEntity(world, pos);
        if (rubble == null)
            return false;

        LootTable lootTable = world.getServer().getLootManager().getTable(StrangeLoot.RUBBLE);
        List<ItemStack> list = lootTable.generateLoot((new LootContext.Builder(world)
            .parameter(LootContextParameters.THIS_ENTITY, entity)
            .parameter(LootContextParameters.ORIGIN, entity.getPos())
            .random(random)
            .build(LootContextTypes.CHEST)));

        if (list.isEmpty())
            return false;

        ItemStack stack;
        if (list.size() == 1 || random.nextFloat() < 0.5F) {
            // fetch from the default loot table
            stack = list.get(0);
        } else {
            // fetch from additional loot tables (legendary items, etc)
            stack = list.get(random.nextInt(list.size() - 1) + 1);
        }

        rubble.setItemStack(stack);
        rubble.markDirty();
        rubble.sync();

        return true;
    }

    private ActionResult level(ServerWorld world, BlockPos pos, BlockState state, int level) {
        world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.setBlockState(pos, state.with(LEVEL, level + 1), 2);

        RubbleBlockEntity rubble = getBlockEntity(world, pos);
        if (rubble == null)
            return ActionResult.FAIL;

        rubble.markDirty();
        rubble.sync();

        return ActionResult.SUCCESS;
    }

    private ActionResult fail(ServerWorld world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 0.9F, 0.9F);
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.65F, 1.17F);

        dropItem(world, pos, new ItemStack(Blocks.GRAVEL));
        world.removeBlockEntity(pos);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);

        return ActionResult.FAIL;
    }

    private ActionResult success(ServerWorld world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return ActionResult.FAIL;

        RubbleBlockEntity rubble = (RubbleBlockEntity)blockEntity;
        ItemStack itemStack = rubble.getItemStack();

        world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0F, 0.9F);
        dropItem(world, pos, itemStack);
        Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)player, new ItemStack(Rubble.RUBBLE));

        world.removeBlockEntity(pos);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);

        return ActionResult.success(true);
    }

    private void levelEffect(World world, BlockPos pos, int level) {
        if (!world.isClient)
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

    private void dropItem(ServerWorld world, BlockPos pos, ItemStack stack) {
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 0.8F);
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, stack);
        world.spawnEntity(entity);
    }

    static {
        LEVEL = Properties.LEVEL_8;
        SHAPES.add(Block.createCuboidShape(0, 0, 0, 16, 16, 16));
        SHAPES.add(Block.createCuboidShape(0, 0, 0, 16, 14, 16));
        SHAPES.add(Block.createCuboidShape(0, 0, 0, 16, 12, 16));
        SHAPES.add(Block.createCuboidShape(0, 0, 0, 16, 10, 16));
        SHAPES.add(Block.createCuboidShape(0, 0, 0, 16, 8, 16));
        SHAPES.add(Block.createCuboidShape(0, 0, 0, 16, 7, 16));
        SHAPES.add(Block.createCuboidShape(0, 0, 0, 16, 6, 16));
        SHAPES.add(Block.createCuboidShape(0, 0, 0, 16, 5, 16));
        SHAPES.add(Block.createCuboidShape(0, 0, 0, 16, 4, 16));
    }
}

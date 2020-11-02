package svenhjol.strange.excavation;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
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
import svenhjol.charm.mixin.accessor.ShovelItemAccessor;
import svenhjol.strange.base.StrangeLoot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AncientRubbleBlock extends CharmBlockWithEntity {
    public static IntProperty LEVEL;
    public static final List<VoxelShape> SHAPES = new ArrayList<>();

    public AncientRubbleBlock(CharmModule module) {
        super(module, "ancient_rubble", AbstractBlock.Settings.of(Material.AGGREGATE, MaterialColor.STONE)
            .strength(8.0F)
            .requiresTool()
            .sounds(BlockSoundGroup.GRAVEL));

        ShovelItemAccessor.getEffectiveBlocks().add(this);
        setDefaultState(getDefaultState().with(LEVEL, 0));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new AncientRubbleBlockEntity();
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

                if (random.nextFloat() < 0.6F)
                    return ActionResult.PASS;

                // get the block entity
                AncientRubbleBlockEntity rubble = getBlockEntity(serverWorld, pos);
                if (rubble == null)
                    return fail(serverWorld, pos);

                // is block supported underneath?
                if (!world.getBlockState(pos.down()).isOpaque())
                    return fail(serverWorld, pos);

                // populate for first time
                if (level == 0 && !populate((ServerWorld) world, pos, player))
                    return fail(serverWorld, pos);

                if (level == 8)
                    return success(serverWorld, pos);

                // test to see if it can be levelled
                boolean shovel = held.getItem() instanceof ShovelItem;
                boolean waited = serverWorld.getTime() - rubble.getLevelTicks() > 50 + random.nextInt(10);
                int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, held);
                int fortune = EnchantmentHelper.getLevel(Enchantments.FORTUNE, held);
                float luck = player.getLuck();

                float chance = 0.15F;
                chance += shovel ? 0.05F : 0;
                chance += waited ? 0.15F : 0;
                chance += efficiency * 0.05F;
                chance += fortune * 0.12F;
                chance += luck * 0.04F;

                if (world.random.nextFloat() > chance)
                    return fail(serverWorld, pos);

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
    public AncientRubbleBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof AncientRubbleBlockEntity))
            return null;

        return (AncientRubbleBlockEntity) blockEntity;
    }

    private boolean populate(ServerWorld world, BlockPos pos, LivingEntity entity) {
        Random random = world.random;

        AncientRubbleBlockEntity rubble = getBlockEntity(world, pos);
        if (rubble == null)
            return false;

        LootTable lootTable = world.getServer().getLootManager().getTable(StrangeLoot.ANCIENT_RUBBLE);
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
        world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_FILL_SUCCESS, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.setBlockState(pos, state.with(LEVEL, level + 1), 2);

        AncientRubbleBlockEntity rubble = getBlockEntity(world, pos);
        if (rubble == null)
            return ActionResult.FAIL;

        rubble.setLevelTicks(world.getTime());
        rubble.markDirty();
        rubble.sync();

        return ActionResult.SUCCESS;
    }

    private ActionResult fail(ServerWorld world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 0.9F);

        dropItem(world, pos, new ItemStack(Blocks.GRAVEL));
        world.removeBlockEntity(pos);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);

        return ActionResult.FAIL;
    }

    private ActionResult success(ServerWorld world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return ActionResult.FAIL;

        AncientRubbleBlockEntity rubble = (AncientRubbleBlockEntity)blockEntity;
        ItemStack itemStack = rubble.getItemStack();

        world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0F, 0.9F);
        dropItem(world, pos, itemStack);

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

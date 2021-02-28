package svenhjol.strange.runicaltars;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
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
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlockWithEntity;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.strange.runestones.Runestones;

import javax.annotation.Nullable;
import java.util.Random;

public class RunicAltarBlock extends CharmBlockWithEntity {
    public static final IntProperty CHARGES;
    public static final VoxelShape TOP;
    public static final VoxelShape MIDDLE;
    public static final VoxelShape BOTTOM;
    public static final VoxelShape COLLISION_SHAPE;
    public static final VoxelShape OUTLINE_SHAPE;

    public RunicAltarBlock(CharmModule module) {
        super(module, RunicAltars.BLOCK_ID.getPath(), Settings.copy(Blocks.STONE)
            .luminance(state -> state.get(CHARGES) > 0 ? 8 : 0));
        this.setDefaultState(getDefaultState().with(CHARGES, 0));
    }

    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int charges = state.get(CHARGES);
        ItemStack held = player.getStackInHand(hand);
        BlockPos destination = getDestinationFromAltar(world, pos);

        if (held.getItem() == Items.CHORUS_FLOWER) {
            if (destination == null)
                return failToApply(world, pos);

            if (charges < 4) {
                if (!world.isClient) {
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.setBlockState(pos, state.with(CHARGES, charges + 1));
                    Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)player, new ItemStack(Blocks.CHORUS_FLOWER)); // to trigger empower_altar advancement
                    held.decrement(1);
                }
            }
            return ActionResult.CONSUME;
        }

        if (destination == null || player.isSneaking()) {
            if (!world.isClient) {
                // ensure client has the latest rune discoveries
                Runestones.sendLearnedRunesPacket((ServerPlayerEntity) player);
                player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            }
        } else {
            if (!world.isClient) {
                if (state.get(CHARGES) > 0) {
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.setBlockState(pos, state.with(CHARGES, charges - 1));
                    Criteria.ENTER_BLOCK.trigger((ServerPlayerEntity) player, state);

                    PlayerHelper.teleport(world, destination, player);
                    world.playSound(null, destination, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.5F, 1.25F);
                } else {
                    return failToApply(world, pos);
                }
            }
        }

        return ActionResult.CONSUME;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        RunicAltarBlockEntity blockEntity = getBlockEntity(world, pos);
        if (blockEntity != null) {
            ItemStack stack = blockEntity.getStack(0);
            ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY() + 0.5D, pos.getZ(), stack);
            world.spawnEntity(itemEntity);
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHARGES);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RunicAltarBlockEntity(pos, state);
    }

    @Nullable
    public RunicAltarBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof RunicAltarBlockEntity) {
            return (RunicAltarBlockEntity)blockEntity;
        }
        return null;
    }

    @Nullable
    private BlockPos getDestinationFromAltar(World world, BlockPos pos) {
        RunicAltarBlockEntity altar = getBlockEntity(world, pos);
        if (altar == null)
            return null;

        return altar.getDestination();
    }

    public static void charge(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state.with(CHARGES, state.get(CHARGES) + 1), 3);
        world.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public ActionResult failToApply(World world, BlockPos pos) {
        if (world.isClient) {
            for (int i = 0; i < 10; i++) {
                double d = (double) pos.getX() + 0.5D + (0.5D - world.random.nextDouble());
                double e = (double) pos.getY() + 1.0D;
                double f = (double) pos.getZ() + 0.5D + (0.5D - world.random.nextDouble());
                double g = (double) world.random.nextFloat() * 0.04D;
                world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0D, g, 0.0D);
            }
        } else {
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        return ActionResult.SUCCESS; // wtf
    }

    // copypasta from RespawnAnchorBlock
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(CHARGES) != 0) {
            double d = (double)pos.getX() + 0.5D + (0.5D - random.nextDouble());
            double e = (double)pos.getY() + 1.0D;
            double f = (double)pos.getZ() + 0.5D + (0.5D - random.nextDouble());
            double g = (double)random.nextFloat() * 0.04D;
            world.addParticle(ParticleTypes.REVERSE_PORTAL, d, e, f, 0.0D, g, 0.0D);
        }
    }

    static {
        CHARGES = Properties.CHARGES;
        TOP = Block.createCuboidShape(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        MIDDLE = Block.createCuboidShape(1.0D, 1.0D, 1.0D, 15.0D, 15.0D, 15.0D);
        BOTTOM = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
        COLLISION_SHAPE = VoxelShapes.union(TOP, MIDDLE, BOTTOM);
        OUTLINE_SHAPE = VoxelShapes.union(TOP, MIDDLE, BOTTOM);
    }
}

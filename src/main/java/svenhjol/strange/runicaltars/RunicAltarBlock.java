package svenhjol.strange.runicaltars;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlockWithEntity;
import svenhjol.strange.runestones.RunestoneHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class RunicAltarBlock extends CharmBlockWithEntity {
    public static final IntProperty CHARGES = Properties.CHARGES;

    public RunicAltarBlock(CharmModule module) {
        super(module, RunicAltars.BLOCK_ID.getPath(), Settings.copy(Blocks.STONE));
        this.setDefaultState(getDefaultState().with(CHARGES, 0));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int charges = state.get(CHARGES);
        ItemStack held = player.getStackInHand(hand);

        if (held.getItem() == Items.CHORUS_FLOWER) {
            RunicAltarBlockEntity altar = getBlockEntity(world, pos);
            if (altar == null)
                return ActionResult.PASS;

            BlockPos destination = altar.getDestination();
            if (destination == null)
                return ActionResult.PASS;

            if (!world.isClient) {
                if (charges < 4) {
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.setBlockState(pos, state.with(CHARGES, charges + 1));
                    held.decrement(1);
                }
                return ActionResult.CONSUME;
            }
        }

        if (charges == 0) {
            if (!world.isClient) {
                // ensure client has the latest rune discoveries
                RunestoneHelper.syncLearnedRunesToClient((ServerPlayerEntity) player);
                player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            }
        } else {
            if (!world.isClient) {
                // TODO: teleport
                world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.setBlockState(pos, state.with(CHARGES, charges - 1));
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
    public BlockEntity createBlockEntity(BlockView world) {
        return new RunicAltarBlockEntity();
    }

    @Nullable
    public RunicAltarBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof RunicAltarBlockEntity) {
            return (RunicAltarBlockEntity)blockEntity;
        }
        return null;
    }

    // copypasta from RespawnAnchorBlock
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(CHARGES) != 0) {
            if (random.nextInt(100) == 0) {
                world.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            double d = (double)pos.getX() + 0.5D + (0.5D - random.nextDouble());
            double e = (double)pos.getY() + 1.0D;
            double f = (double)pos.getZ() + 0.5D + (0.5D - random.nextDouble());
            double g = (double)random.nextFloat() * 0.04D;
            world.addParticle(ParticleTypes.REVERSE_PORTAL, d, e, f, 0.0D, g, 0.0D);
        }
    }
}

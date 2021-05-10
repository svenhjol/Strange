package svenhjol.strange.storagecrates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlockWithEntity;
import svenhjol.charm.base.enums.IVariantMaterial;
import svenhjol.charm.base.helper.PlayerHelper;

import javax.annotation.Nullable;

public class StorageCrateBlock extends CharmBlockWithEntity {
    public static final DirectionProperty FACING = Properties.FACING;
    private static final VoxelShape SHAPE = createCuboidShape(1.0D, 1.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public StorageCrateBlock(CharmModule module, IVariantMaterial material) {
        super(module, material.asString() + "_storage_crate",
            Settings.copy(Blocks.COMPOSTER)
        );
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);
        boolean isCreative = player.getAbilities().creativeMode;
        boolean isSneaking = player.isSneaking();

        StorageCrateBlockEntity crate = getBlockEntity(world, pos);
        if (crate != null) {
            boolean performTransfer = false;

            if (!world.isClient) {
                if (crate.count > 0 && isSneaking) {
                    int amountToRemove = Math.min(crate.item.getMaxCount(), crate.count);

                    if (!isCreative) {
                        PlayerHelper.addOrDropStack(player, new ItemStack(crate.item, amountToRemove));
                    }

                    crate.count -= amountToRemove;

                    if (crate.count == 0)
                        crate.item = null;

                    performTransfer = true;

                } else if (!held.isEmpty()) {
                    int amountToAdd = held.getCount();

                    if (crate.item == held.getItem()) {
                        performTransfer = true;

                    } else if (crate.count == 0) {
                        crate.item = held.getItem();
                        performTransfer = true;
                    }

                    if (performTransfer) {
                        crate.count += amountToAdd;

                        if (!isCreative)
                            held.decrement(amountToAdd);
                    }
                }

                if (performTransfer) {
                    crate.markDirty();
                    crate.sync();
                }
            }

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        StorageCrateBlockEntity crate = getBlockEntity(world, pos);
        if (crate != null) {
            DefaultedList<ItemStack> stacks = DefaultedList.of();
            int numStacks = crate.count / StorageCrates.maximumStacks;
            int remainder = crate.count % crate.item.getMaxCount();

            if (numStacks > 0) {
                for (int i = 0; i < numStacks - 1; i++) {
                    stacks.add(new ItemStack(crate.item, crate.item.getMaxCount()));
                }
            }
            stacks.add(new ItemStack(crate.item, remainder));
            ItemScatterer.spawn(world, pos, stacks);
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    public StorageCrateBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof StorageCrateBlockEntity)
            return (StorageCrateBlockEntity) blockEntity;

        return null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StorageCrateBlockEntity(pos, state);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}

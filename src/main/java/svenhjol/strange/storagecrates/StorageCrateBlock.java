package svenhjol.strange.storagecrates;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlockWithEntity;

import javax.annotation.Nullable;

public class StorageCrateBlock extends CharmBlockWithEntity {
    private static final VoxelShape RAY_TRACE_SHAPE = createCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape OUTLINE_SHAPE;

    public StorageCrateBlock(CharmModule module) {
        super(module, "storage_crate", Settings.copy(Blocks.COMPOSTER));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return RAY_TRACE_SHAPE;
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);

        StorageCrateBlockEntity crate = getBlockEntity(world, pos);
        if (crate != null) {
            boolean didThing = false;

            if (!world.isClient) {
                if (crate.count > 0 && held.isEmpty()) {
                    --crate.count;
                    if (crate.count == 0)
                        crate.item = null;

                    didThing = true;
                } else if (!held.isEmpty()) {
                    if (crate.item == held.getItem()) {
                        ++crate.count;
                    } else {
                        crate.item = held.getItem();
                        crate.count = 1;
                    }
                    didThing = true;
                }
                if (didThing) {
                    crate.markDirty();
                    crate.sync();

                }
            }

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
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

    static {
        OUTLINE_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.union(createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), createCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), RAY_TRACE_SHAPE), BooleanBiFunction.ONLY_FIRST);
    }
}

package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import svenhjol.meson.MesonModule;
import svenhjol.strange.runestones.module.RunePortals;

import javax.annotation.Nullable;

public class PortalRunestoneBlock extends BaseRunestoneBlock {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    public PortalRunestoneBlock(MesonModule module, int runeValue) {
        super(module, "portal_runestone", runeValue, Properties.from(Blocks.OBSIDIAN));

        this.setDefaultState(this.getStateContainer().getBaseState()
            .with(COLOR, DyeColor.WHITE)
            .with(FACING, Direction.NORTH)
        );
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        final ItemStack held = player.getHeldItem(handIn);

        if (!worldIn.isRemote
            && !held.isEmpty()
        ) {
            final Item item = held.getItem();
            if (item instanceof DyeItem) {
                final DyeItem dye = (DyeItem)item;
                final DyeColor dyeColor = dye.getDyeColor();

                final BlockState newState = state
                    .with(COLOR, dyeColor);

                worldIn.setBlockState(pos, newState, 2);
                worldIn.playSound(null, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.PLAYERS, 1.0F, 1.0F);

                if (!player.isCreative())
                    held.shrink(1);

                return true;
            }
        }

        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (world.isRemote)
            return;

        RunePortals.breakSurroundingPortals(world, pos);
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(COLOR, FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.toRotation(state.get(FACING)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState()
            .with(COLOR, DyeColor.WHITE)
            .with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    public static DyeColor getRuneColor(BlockState state) {
        if (!(state.getBlock() instanceof PortalRunestoneBlock))
            return null;

        final DyeColor dyeColor = state.get(PortalRunestoneBlock.COLOR);
        return dyeColor;
    }
}

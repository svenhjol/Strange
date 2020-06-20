package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import svenhjol.meson.MesonModule;
import svenhjol.strange.runestones.module.RunePortals;

import javax.annotation.Nullable;

public class RunicAmethystBlock extends BaseRunestoneBlock {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    public RunicAmethystBlock(MesonModule module, int runeValue) {
        super(module, "runic_amethyst", runeValue, Properties.from(Blocks.OBSIDIAN));

        this.setDefaultState(this.getStateContainer().getBaseState()
            .with(FACING, Direction.NORTH)
        );
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
        builder.add(FACING);
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
            .with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        return 1;
    }
}

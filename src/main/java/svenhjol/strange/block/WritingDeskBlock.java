package svenhjol.strange.block;

import net.minecraft.block.*;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;
import svenhjol.strange.module.Scrollkeepers;

import javax.annotation.Nullable;

public class WritingDeskBlock extends MesonBlock {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public WritingDeskBlock(MesonModule module) {
        super(module, Scrollkeepers.BLOCK_ID.getPath(), AbstractBlock.Settings.copy(Blocks.CARTOGRAPHY_TABLE));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

    @Override
    public ItemGroup getItemGroup() {
        return ItemGroup.DECORATIONS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }
}

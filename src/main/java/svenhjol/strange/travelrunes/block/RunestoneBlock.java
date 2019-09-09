package svenhjol.strange.travelrunes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public class RunestoneBlock extends MesonBlock
{
    public static final IntegerProperty RUNE = IntegerProperty.create("rune", 0, 11);
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 2);

    public RunestoneBlock(MesonModule module)
    {
        super(module, "runestone", Block.Properties.from(Blocks.STONE));
        setDefaultState(getStateContainer().getBaseState().with(RUNE, 0));
        setDefaultState(getStateContainer().getBaseState().with(TYPE, 0));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(RUNE, TYPE);
    }
}

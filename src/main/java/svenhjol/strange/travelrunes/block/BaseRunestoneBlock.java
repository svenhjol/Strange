package svenhjol.strange.travelrunes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public abstract class BaseRunestoneBlock extends MesonBlock
{
    public static final IntegerProperty RUNE = IntegerProperty.create("rune", 0, 11);

    public BaseRunestoneBlock(MesonModule module, String name, Block.Properties props)
    {
        super(module, name, props);
        setDefaultState(getStateContainer().getBaseState().with(RUNE, 0));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(RUNE);
    }
}

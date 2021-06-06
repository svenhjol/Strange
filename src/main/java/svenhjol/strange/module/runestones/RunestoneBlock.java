package svenhjol.strange.module.runestones;

import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.module.CharmModule;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class RunestoneBlock extends CharmBlockWithEntity {
    private final int runeValue;

    public RunestoneBlock(CharmModule module, int runeValue) {
        super(module, "runestone_" + runeValue, BlockBehaviour.Properties.copy(Blocks.STONE));
        this.runeValue = runeValue;
    }

    public int getRuneValue() {
        return runeValue;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }
}

package svenhjol.strange.module.runestones;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.module.CharmModule;

import javax.annotation.Nullable;

public class RunestoneBlock extends CharmBlockWithEntity {
    private final int runeValue;

    public RunestoneBlock(CharmModule module, int runeValue) {
        super(module, "runestone_" + runeValue, AbstractBlock.Settings.copy(Blocks.STONE));
        this.runeValue = runeValue;
    }

    public int getRuneValue() {
        return runeValue;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }
}

package svenhjol.strange.runestones;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlockWithEntity;

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
    public BlockEntity createBlockEntity(BlockView world) {
        return new RunestoneBlockEntity();
    }
}

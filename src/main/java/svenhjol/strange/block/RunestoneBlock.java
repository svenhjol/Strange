package svenhjol.strange.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public class RunestoneBlock extends MesonBlock {
    private final int runeValue;

    public RunestoneBlock(MesonModule module, int runeValue) {
        super(module, "runestone_" + runeValue, AbstractBlock.Settings.copy(Blocks.STONE));
        this.runeValue = runeValue;
    }

    public int getRuneValue() {
        return runeValue;
    }
}

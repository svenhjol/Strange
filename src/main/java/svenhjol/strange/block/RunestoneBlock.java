package svenhjol.strange.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlockWithEntity;
import svenhjol.strange.blockentity.RunestoneBlockEntity;

import javax.annotation.Nullable;

public class RunestoneBlock extends MesonBlockWithEntity {
    private final int runeValue;

    public RunestoneBlock(MesonModule module, int runeValue) {
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

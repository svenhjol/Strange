package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public class AmethystBlock extends MesonBlock {
    public AmethystBlock(MesonModule module) {
        super(module, "amethyst_block", Block.Properties.from(Blocks.DIAMOND_BLOCK));
    }
}

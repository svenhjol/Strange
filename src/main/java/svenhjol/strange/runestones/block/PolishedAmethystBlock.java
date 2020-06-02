package svenhjol.strange.runestones.block;

import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public class PolishedAmethystBlock extends MesonBlock {
    public PolishedAmethystBlock(MesonModule module) {
        super(module, "polished_amethyst", Properties.from(Blocks.DIAMOND_BLOCK));
    }
}

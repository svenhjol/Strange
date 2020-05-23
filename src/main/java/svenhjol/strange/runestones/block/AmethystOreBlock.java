package svenhjol.strange.runestones.block;

import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public class AmethystOreBlock extends MesonBlock {
    public AmethystOreBlock(MesonModule module) {
        super(module, "amethyst_ore", Properties.from(Blocks.DIAMOND_ORE));
    }
}

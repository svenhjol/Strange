package svenhjol.strange.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.StairsBlock;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.IMesonBlock;
import svenhjol.strange.module.Amethyst;

public class AmethystStairsBlock extends StairsBlock implements IMesonBlock {
    private MesonModule module;

    public AmethystStairsBlock(MesonModule module) {
        super(Amethyst.AMETHYST.getDefaultState(), AbstractBlock.Settings.copy(Amethyst.AMETHYST));
        this.register(module, "amethyst_stairs");
        this.module = module;
    }

    @Override
    public boolean enabled() {
        return module.enabled;
    }
}

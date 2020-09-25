package svenhjol.strange.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SlabBlock;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.IMesonBlock;
import svenhjol.strange.module.Amethyst;

public class AmethystSlabBlock extends SlabBlock implements IMesonBlock {
    private MesonModule module;

    public AmethystSlabBlock(MesonModule module) {
        super(AbstractBlock.Settings.copy(Amethyst.AMETHYST));
        this.register(module, "amethyst_slab");
        this.module = module;
    }

    @Override
    public boolean enabled() { return module.enabled;
   }
}

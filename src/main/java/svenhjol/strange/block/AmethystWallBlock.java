package svenhjol.strange.block;


import net.minecraft.block.AbstractBlock;
import net.minecraft.block.WallBlock;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.IMesonBlock;
import svenhjol.strange.module.Amethyst;

public class AmethystWallBlock  extends WallBlock implements IMesonBlock {
    private MesonModule module;

    public AmethystWallBlock(MesonModule module) {
        super(AbstractBlock.Settings.copy(Amethyst.AMETHYST));
        this.register(module, "amethyst_wall");
        this.module = module;
    }

    @Override
    public boolean enabled() {
        return module.enabled;
    }
}

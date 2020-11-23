package svenhjol.strange.runicaltars;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.RenderLayer;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;

public class RunicAltarsClient extends CharmClientModule {
    public RunicAltarsClient(CharmModule module) {
        super(module);
    }

    @Override
    public void init() {
        ScreenRegistry.register(RunicAltars.SCREEN_HANDLER, RunicAltarScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(RunicAltars.RUNIC_ALTAR, RenderLayer.getCutout());
    }
}

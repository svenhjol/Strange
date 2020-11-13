package svenhjol.strange.writingdesks;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.RenderLayer;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;

public class WritingDesksClient extends CharmClientModule {
    public WritingDesksClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(WritingDesks.WRITING_DESK, RenderLayer.getCutout());
    }

    @Override
    public void init() {
        ScreenRegistry.register(WritingDesks.SCREEN_HANDLER, WritingDeskScreen::new);
    }
}

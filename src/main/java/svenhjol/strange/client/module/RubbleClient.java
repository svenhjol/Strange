package svenhjol.strange.client.module;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;
import svenhjol.strange.client.render.block.entity.RubbleBlockEntityRenderer;
import svenhjol.strange.module.Rubble;

public class RubbleClient extends CharmClientModule {
    public RubbleClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(Rubble.RUBBLE, RenderLayer.getCutout());
        BlockEntityRendererRegistry.INSTANCE.register(Rubble.BLOCK_ENTITY, RubbleBlockEntityRenderer::new);
    }
}

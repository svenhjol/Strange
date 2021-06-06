package svenhjol.strange.module.rubble;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import svenhjol.charm.module.CharmClientModule;
import svenhjol.charm.module.CharmModule;

public class RubbleClient extends CharmClientModule {
    public RubbleClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(Rubble.RUBBLE, RenderType.cutout());
        BlockEntityRendererRegistry.INSTANCE.register(Rubble.BLOCK_ENTITY, RubbleBlockEntityRenderer::new);
    }
}

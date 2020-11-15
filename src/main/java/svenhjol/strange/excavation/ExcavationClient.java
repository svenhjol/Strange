package svenhjol.strange.excavation;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;

public class ExcavationClient extends CharmClientModule {
    public ExcavationClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(Excavation.ANCIENT_RUBBLE, RenderLayer.getCutout());
        BlockEntityRendererRegistry.INSTANCE.register(Excavation.BLOCK_ENTITY, AncientRubbleBlockEntityRenderer::new);
    }
}

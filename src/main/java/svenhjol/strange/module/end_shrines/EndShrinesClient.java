package svenhjol.strange.module.end_shrines;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

@ClientModule(module = EndShrines.class)
public class EndShrinesClient extends CharmModule {
    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(EndShrines.END_SHRINE_PORTAL_BLOCK, RenderType.translucent());
        BlockEntityRendererRegistry.register(EndShrines.END_SHRINE_PORTAL_BLOCK_ENTITY, EndShrinePortalBlockEntityRenderer::new);
    }
}

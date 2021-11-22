package svenhjol.strange.module.rubble;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

@ClientModule(module = Rubble.class)
public class RubbleClient extends CharmModule {
    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(Rubble.RUBBLE, RenderType.cutout());
        BlockEntityRendererRegistry.register(Rubble.BLOCK_ENTITY, RubbleBlockEntityRenderer::new);
    }
}

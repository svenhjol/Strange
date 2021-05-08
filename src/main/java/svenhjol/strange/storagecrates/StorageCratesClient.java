package svenhjol.strange.storagecrates;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;

public class StorageCratesClient extends CharmClientModule {
    public StorageCratesClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(StorageCrates.STORAGE_CRATE, RenderLayer.getCutout());
        BlockEntityRendererRegistry.INSTANCE.register(StorageCrates.BLOCK_ENTITY, StorageCrateBlockEntityRenderer::new);
    }
}

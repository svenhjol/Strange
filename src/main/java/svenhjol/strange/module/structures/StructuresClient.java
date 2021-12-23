package svenhjol.strange.module.structures;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.structures.network.ClientReceiveOpenDataBlockScreen;
import svenhjol.strange.module.structures.network.ClientReceiveOpenEntityBlockScreen;
import svenhjol.strange.module.structures.network.ClientSendUpdateStructureBlock;

@ClientModule(module = Structures.class)
public class StructuresClient extends CharmModule {
    public static ClientReceiveOpenDataBlockScreen CLIENT_RECEIVE_OPEN_DATA_BLOCK_SCREEN;
    public static ClientReceiveOpenEntityBlockScreen CLIENT_RECEIVE_OPEN_ENTITY_BLOCK_SCREEN;
    public static ClientSendUpdateStructureBlock CLIENT_SEND_UPDATE_STRUCTURE_BLOCK;

    @Override
    public void register() {
        CLIENT_RECEIVE_OPEN_DATA_BLOCK_SCREEN = new ClientReceiveOpenDataBlockScreen();
        CLIENT_RECEIVE_OPEN_ENTITY_BLOCK_SCREEN = new ClientReceiveOpenEntityBlockScreen();
        CLIENT_SEND_UPDATE_STRUCTURE_BLOCK = new ClientSendUpdateStructureBlock();

        BlockRenderLayerMap.INSTANCE.putBlock(Structures.DATA_BLOCK, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Structures.ENTITY_BLOCK, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Structures.IGNORE_BLOCK, RenderType.translucent());

        BlockEntityRendererRegistry.register(Structures.DATA_BLOCK_ENTITY, DataBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(Structures.ENTITY_BLOCK_ENTITY, EntityBlockEntityRenderer::new);
    }
}

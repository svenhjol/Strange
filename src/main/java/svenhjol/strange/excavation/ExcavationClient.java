package svenhjol.strange.excavation;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import svenhjol.charm.base.CharmModule;
import svenhjol.strange.module.Excavation;

public class ExcavationClient {
    public ExcavationClient(CharmModule module) {
        BlockEntityRendererRegistry.INSTANCE.register(Excavation.BLOCK_ENTITY, AncientRubbleBlockEntityRenderer::new);
    }
}

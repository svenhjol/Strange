package svenhjol.strange.feature.chairs;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm_core.annotation.ClientFeature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeClient;

import java.util.List;
import java.util.function.BooleanSupplier;

@ClientFeature
public class ChairsClient extends CharmFeature {
    @Override
    public List<BooleanSupplier> checks() {
        return List.of(() -> Strange.LOADER.isEnabled(Chairs.class));
    }

    @Override
    public void register() {
        StrangeClient.REGISTRY.entityRenderer(Chairs.ENTITY, () -> ChairEntityRenderer::new);
    }

    static class ChairEntityRenderer extends EntityRenderer<ChairEntity> {
        public ChairEntityRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(ChairEntity entity) {
            return null;
        }
    }
}

package svenhjol.strange.feature.totem_of_preserving;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import svenhjol.charm_core.annotation.ClientFeature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeClient;

import java.util.List;
import java.util.function.BooleanSupplier;

@ClientFeature
public class TotemOfPreservingClient extends CharmFeature {
    @Override
    public List<BooleanSupplier> checks() {
        return List.of(() -> Strange.LOADER.isEnabled(TotemOfPreserving.class));
    }

    @Override
    public void register() {
        StrangeClient.REGISTRY.blockEntityRenderer(TotemOfPreserving.BLOCK_ENTITY, () -> TotemBlockEntityRenderer::new);
        StrangeClient.REGISTRY.blockRenderType(TotemOfPreserving.BLOCK, RenderType::translucent);

        if (isEnabled()) {
            StrangeClient.REGISTRY.itemTab(
                TotemOfPreserving.ITEM,
                CreativeModeTabs.COMBAT,
                Items.TOTEM_OF_UNDYING
            );
        }
    }
}

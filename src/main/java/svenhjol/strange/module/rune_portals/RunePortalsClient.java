package svenhjol.strange.module.rune_portals;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.DyeColor;
import svenhjol.charm.module.CharmClientModule;
import svenhjol.charm.module.CharmModule;

public class RunePortalsClient extends CharmClientModule {
    public RunePortalsClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(RunePortals.RUNE_PORTAL_BLOCK, RenderLayer.getTranslucent());

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null) {
                if (state.contains(RunePortalBlock.COLOR)) {
                    int colorId = state.get(RunePortalBlock.COLOR);
                    DyeColor color = DyeColor.byId(colorId);
                    return color.getSignColor();
                }
            }
            return 0xFFFFFF;
        }, RunePortals.RUNE_PORTAL_BLOCK);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null) {
                // the edge of the rune
                if (tintIndex == 0) {
                    return 0x222222;
                }

                // the face of the rune
                if (tintIndex == 1) {
                    return 0x6C4F70;
                }
            }
            return 0xFFFFFF;
        }, RunePortals.FRAME_BLOCK);
//        BlockEntityRendererRegistry.INSTANCE.register(RunePortals.RUNE_PORTAL_BLOCK_ENTITY, RunePortalBlockEntityRenderer::new);
    }
}

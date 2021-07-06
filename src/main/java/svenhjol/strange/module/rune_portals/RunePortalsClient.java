package svenhjol.strange.module.rune_portals;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.DyeColor;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

@ClientModule(module = RunePortals.class)
public class RunePortalsClient extends CharmModule {
    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(RunePortals.RUNE_PORTAL_BLOCK, RenderType.translucent());

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null) {
                if (state.hasProperty(RunePortalBlock.COLOR)) {
                    int colorId = state.getValue(RunePortalBlock.COLOR);
                    DyeColor color = DyeColor.byId(colorId);
                    return color.getTextColor();
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
                    return 0xBC9AD0;
                }
            }
            return 0xFFFFFF;
        }, RunePortals.PORTAL_FRAME_BLOCK);
//        BlockEntityRendererRegistry.INSTANCE.register(RunePortals.RUNE_PORTAL_BLOCK_ENTITY, RunePortalBlockEntityRenderer::new);
    }
}

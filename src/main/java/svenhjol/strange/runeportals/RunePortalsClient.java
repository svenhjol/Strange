package svenhjol.strange.runeportals;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.DyeColor;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;

public class RunePortalsClient extends CharmClientModule {
    public RunePortalsClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(RunePortals.RUNE_PORTAL_BLOCK, RenderLayer.getTranslucent());

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof RunePortalBlockEntity) {
                    RunePortalBlockEntity portal = (RunePortalBlockEntity)blockEntity;
                    return DyeColor.byId(portal.color).getSignColor();
                }
            }
            return 0xFFFFFF;
        }, RunePortals.RUNE_PORTAL_BLOCK);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null) {
                // the edge of the rune
                if (tintIndex == 0) {
                    return 0xFFFFFF;
                }

                // the face of the rune
                if (tintIndex == 1) {
                    return 0xFFFFFF;
                }
            }
            return 0xFFFFFF;
        }, RunePortals.FRAME_BLOCK);
//        BlockEntityRendererRegistry.INSTANCE.register(RunePortals.RUNE_PORTAL_BLOCK_ENTITY, RunePortalBlockEntityRenderer::new);
    }
}

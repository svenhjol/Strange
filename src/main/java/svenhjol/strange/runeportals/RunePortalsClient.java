package svenhjol.strange.runeportals;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;

import java.awt.*;
import java.util.Random;

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

                    Random rand = new Random(portal.hash);
                    float r = rand.nextFloat();
                    float g = rand.nextFloat();
                    float b = rand.nextFloat();

                    return new Color(r, g, b).brighter().getRGB();
//                    DyeColor dye = DyeColor.byId(((RunePortalBlockEntity)blockEntity).color);
//                    return dye.getSignColor();
                }
            }
            return 0xffffff;
        }, RunePortals.RUNE_PORTAL_BLOCK);
//        BlockEntityRendererRegistry.INSTANCE.register(RunePortals.RUNE_PORTAL_BLOCK_ENTITY, RunePortalBlockEntityRenderer::new);
    }
}

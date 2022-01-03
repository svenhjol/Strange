package svenhjol.strange.module.runestone_dust;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RunestoneDustEntityRenderer extends EntityRenderer<RunestoneDustEntity> {
    public RunestoneDustEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(RunestoneDustEntity entity) {
        return null;
    }
}

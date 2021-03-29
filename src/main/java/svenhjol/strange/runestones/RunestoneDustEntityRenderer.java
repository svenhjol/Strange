package svenhjol.strange.runestones;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class RunestoneDustEntityRenderer extends EntityRenderer<RunestoneDustEntity> {
    public RunestoneDustEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(RunestoneDustEntity entity) {
        return null;
    }
}

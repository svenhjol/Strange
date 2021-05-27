package svenhjol.strange.client.render.entity;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import svenhjol.strange.entity.RunestoneDustEntity;

public class RunestoneDustEntityRenderer extends EntityRenderer<RunestoneDustEntity> {
    public RunestoneDustEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(RunestoneDustEntity entity) {
        return null;
    }
}

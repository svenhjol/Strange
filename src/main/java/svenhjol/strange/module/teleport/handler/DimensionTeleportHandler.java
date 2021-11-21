package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.knowledge.KnowledgeBranch;

public class DimensionTeleportHandler extends TeleportHandler<ResourceLocation> {
    public DimensionTeleportHandler(KnowledgeBranch<?, ResourceLocation> branch) {
        super(branch);
    }

    @Override
    public void process() {
        BlockPos target = entity.blockPosition();
        ResourceLocation dimension = value;
        tryTeleport(dimension, target, false, true);
    }
}

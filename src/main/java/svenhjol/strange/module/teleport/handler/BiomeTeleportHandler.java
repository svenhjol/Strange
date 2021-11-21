package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.knowledge.KnowledgeBranch;

public class BiomeTeleportHandler extends TeleportHandler<ResourceLocation> {
    public BiomeTeleportHandler(KnowledgeBranch<?, ResourceLocation> branch) {
        super(branch);
    }

    @Override
    public void process() {
        BlockPos target = getBiomeTarget(value, level, originPos);
        ResourceLocation dimension = level.dimension().location();
        tryTeleport(dimension, target,false, false);
    }
}

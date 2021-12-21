package svenhjol.strange.module.teleport.runic.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.runes.RuneBranch;

public class DimensionTeleportHandler extends BaseTeleportHandler<ResourceLocation> {
    public DimensionTeleportHandler(RuneBranch<?, ResourceLocation> branch) {
        super(branch);
    }

    @Override
    public boolean process() {
        BlockPos target = entity.blockPosition();
        ResourceLocation dimension = value;
        return teleport(dimension, target, false, true);
    }
}

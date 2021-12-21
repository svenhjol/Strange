package svenhjol.strange.module.teleport.runic.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.runes.RuneBranch;

public class StructureTeleportHandler extends BaseTeleportHandler<ResourceLocation> {
    public StructureTeleportHandler(RuneBranch<?, ResourceLocation> branch) {
        super(branch);
    }

    @Override
    public boolean process() {
        BlockPos target = getStructureTarget(value, level, originPos);
        ResourceLocation dimension = level.dimension().location();
        return teleport(dimension, target, false, false);
    }
}

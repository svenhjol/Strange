package svenhjol.strange.module.teleport.runic.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.runes.RuneBranch;

public class BiomeTeleportHandler extends BaseTeleportHandler<ResourceLocation> {
    public BiomeTeleportHandler(RuneBranch<?, ResourceLocation> branch) {
        super(branch);
    }

    @Override
    public void process() {
        BlockPos target = getBiomeTarget(value, level, originPos);
        ResourceLocation dimension = level.dimension().location();
        tryTeleport(dimension, target,false, false);
    }
}

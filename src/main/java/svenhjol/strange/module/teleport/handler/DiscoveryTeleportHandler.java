package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.module.discoveries.Discovery;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runestones.Runestones;

public class DiscoveryTeleportHandler extends TeleportHandler<Discovery> {
    public DiscoveryTeleportHandler(RuneBranch<?, Discovery> branch) {
        super(branch);
    }

    @Override
    public void process() {
        ResourceLocation id = value.getLocation();
        ResourceLocation dimension = value.getDimension();
        BlockPos target;

        if (id.equals(Runestones.SPAWN)) {
            target = level.getSharedSpawnPos();
            dimension = ServerLevel.OVERWORLD.location();
        } else if (WorldHelper.isStructure(id)) {
            target = getStructureTarget(id, level, originPos);
        } else if (WorldHelper.isBiome(id)) {
            target = getBiomeTarget(id, level, originPos);
        } else {
            return;
        }

        tryTeleport(dimension, target, false, false);
    }
}

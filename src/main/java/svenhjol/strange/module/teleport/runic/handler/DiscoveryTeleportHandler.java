package svenhjol.strange.module.teleport.runic.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.module.discoveries.Discovery;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runestones.Runestones;

public class DiscoveryTeleportHandler extends BaseTeleportHandler<Discovery> {
    public DiscoveryTeleportHandler(RuneBranch<?, Discovery> branch) {
        super(branch);
    }

    @Override
    public boolean process() {
        var id = value.getLocation();
        var dimension = value.getDimension();
        var pos = value.getPos();

        if (pos == null) {
            pos = originPos;
        }

        BlockPos target;
        boolean allowDimensionChange = false;

        if (id.equals(Runestones.SPAWN)) {
            target = level.getSharedSpawnPos();
            dimension = ServerLevel.OVERWORLD.location();
            allowDimensionChange = true;
        } else if (WorldHelper.isStructure(id)) {
            target = getStructureTarget(id, level, pos);
        } else if (WorldHelper.isBiome(id)) {
            target = getBiomeTarget(id, level, pos);
        } else {
            return false;
        }

        return teleport(dimension, target, false, allowDimensionChange);
    }
}

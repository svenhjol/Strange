package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.types.Discovery;
import svenhjol.strange.module.runestones.RunestoneLocations;

public class SpecialTeleportHandler extends TeleportHandler<Discovery> {
    public SpecialTeleportHandler(KnowledgeBranch<?, Discovery> branch) {
        super(branch);
    }

    @Override
    public void process() {
        ResourceLocation id = value.getId();
        ResourceLocation dimension = null;
        BlockPos target = null;

        if (id.equals(RunestoneLocations.SPAWN)) {
            target = level.getSharedSpawnPos();
            dimension = ServerLevel.OVERWORLD.location();
        }

        if (target == null || dimension == null) {
            return;
        }

        tryTeleport(dimension, target, false, true);
    }
}

package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.types.Discovery;

public class DiscoveryTeleportHandler extends TeleportHandler<Discovery> {
    public DiscoveryTeleportHandler(KnowledgeBranch<?, Discovery> branch) {
        super(branch);
    }

    @Override
    public void process() {
        ResourceLocation id = value.getId();
        ResourceLocation dimension = value.getDimension().orElseThrow();
        BlockPos target;

        if (WorldHelper.isStructure(id)) {
            target = getStructureTarget(id, level, originPos);
        } else if (WorldHelper.isBiome(id)) {
            target = getBiomeTarget(id, level, originPos);
        } else {
            return;
        }

        tryTeleport(dimension, target, false, false);
    }
}

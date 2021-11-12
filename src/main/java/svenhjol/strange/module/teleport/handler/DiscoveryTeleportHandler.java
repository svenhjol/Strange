package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.types.Discovery;

public class DiscoveryTeleportHandler extends TeleportHandler<Discovery> {
    public DiscoveryTeleportHandler(KnowledgeBranch<?, Discovery> branch, ServerLevel level, LivingEntity entity, ItemStack sacrifice, String runes, BlockPos origin) {
        super(branch, level, entity, sacrifice, runes, origin);
    }

    @Override
    public void process() {
        ResourceLocation id = value.getId();
        dimension = value.getDimension().orElseThrow();

        if (WorldHelper.isStructure(id)) {
            target = getStructureTarget(id, level, origin);
        } else if (WorldHelper.isBiome(id)) {
            target = getBiomeTarget(id, level, origin);
        }

        if (checkAndApplyEffects()) {
            teleport(false, false);
        }
    }
}

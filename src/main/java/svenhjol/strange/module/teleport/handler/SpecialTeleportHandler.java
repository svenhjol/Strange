package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.types.Discovery;
import svenhjol.strange.module.runestones.RunestoneLocations;

public class SpecialTeleportHandler extends TeleportHandler<Discovery> {
    public SpecialTeleportHandler(KnowledgeBranch<?, Discovery> branch, ServerLevel level, LivingEntity entity, ItemStack sacrifice, String runes, BlockPos origin) {
        super(branch, level, entity, sacrifice, runes, origin);
    }

    @Override
    public void process() {
        ResourceLocation id = value.getId();
        dimension = ServerLevel.OVERWORLD.location();

        if (id.equals(RunestoneLocations.SPAWN)) {
            target = level.getSharedSpawnPos();
        }

        if (checkAndApplyEffects()) {
            teleport(false, true);
        }
    }
}

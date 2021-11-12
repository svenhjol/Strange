package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.knowledge.KnowledgeBranch;

public class BiomeTeleportHandler extends TeleportHandler<ResourceLocation> {
    public BiomeTeleportHandler(KnowledgeBranch<?, ResourceLocation> branch, ServerLevel level, LivingEntity entity, ItemStack sacrifice, String runes, BlockPos origin) {
        super(branch, level, entity, sacrifice, runes, origin);
    }

    @Override
    public void process() {
        target = getBiomeTarget(value, level, origin);
        dimension = level.dimension().location();

        if (checkAndApplyEffects()) {
            teleport(false, false);
        }
    }
}

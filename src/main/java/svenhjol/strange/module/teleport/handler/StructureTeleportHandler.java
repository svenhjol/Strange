package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.runestones.RunestoneHelper;

import java.util.List;

public class StructureTeleportHandler extends TeleportHandler<ResourceLocation> {
    public StructureTeleportHandler(KnowledgeBranch<?, ResourceLocation> branch, ServerLevel level, LivingEntity entity, ItemStack sacrifice, String runes, BlockPos origin) {
        super(branch, level, entity, sacrifice, runes, origin);
    }

    @Override
    public void process() {
        target = getStructureTarget(value, level, origin);
        dimension = level.dimension().location();
        List<Item> items = RunestoneHelper.getItems(dimension, runes);

        if (checkAndApplyEffects(items)) {
            teleport(false, false);
        } else {
            badThings();
        }
    }
}

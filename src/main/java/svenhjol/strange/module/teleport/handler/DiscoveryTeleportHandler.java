package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.types.Discovery;
import svenhjol.strange.module.runestones.RunestoneHelper;

import java.util.List;

public class DiscoveryTeleportHandler extends TeleportHandler<Discovery> {
    public DiscoveryTeleportHandler(KnowledgeBranch<?, Discovery> branch, ServerLevel level, LivingEntity entity, ItemStack sacrifice, String runes, BlockPos origin) {
        super(branch, level, entity, sacrifice, runes, origin);
    }

    @Override
    public void process() {
        ResourceLocation id = value.getId();
        dimension = value.getDimension().orElseThrow();
        List<Item> items = RunestoneHelper.getItems(dimension, runes);

        if (WorldHelper.isStructure(id)) {
            target = getStructureTarget(id, level, origin);
        } else if (WorldHelper.isBiome(id)) {
            target = getBiomeTarget(id, level, origin);
        }

        if (checkAndApplyEffects(items)) {
            teleport(false, false);
        } else {
            badThings();
        }
    }
}

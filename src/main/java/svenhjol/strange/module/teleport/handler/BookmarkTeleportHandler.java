package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.runestones.RunestoneHelper;

import java.util.List;

public class BookmarkTeleportHandler extends TeleportHandler<JournalBookmark> {
    public BookmarkTeleportHandler(KnowledgeBranch<?, JournalBookmark> branch, ServerLevel level, LivingEntity entity, ItemStack sacrifice, String runes, BlockPos origin) {
        super(branch, level, entity, sacrifice, runes, origin);
    }

    @Override
    public void process() {
        target = value.getBlockPos();
        dimension = value.getDimension();
        List<Item> items = RunestoneHelper.getItems(dimension, runes);

        if (checkAndApplyEffects(items)) {
            teleport(true, false);
        } else {
            badThings();
        }
    }
}

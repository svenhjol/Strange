package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.knowledge.KnowledgeBranch;

public class BookmarkTeleportHandler extends TeleportHandler<JournalBookmark> {
    public BookmarkTeleportHandler(KnowledgeBranch<?, JournalBookmark> branch, ServerLevel level, LivingEntity entity, ItemStack sacrifice, String runes, BlockPos originPos) {
        super(branch, level, entity, sacrifice, runes, originPos);
    }

    @Override
    public void process() {
        BlockPos target = value.getBlockPos();
        ResourceLocation dimension  = value.getDimension();
        tryTeleport(dimension, target, true, false);
    }
}

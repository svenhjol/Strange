package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.knowledge.KnowledgeBranch;

public class BookmarkTeleportHandler extends TeleportHandler<JournalBookmark> {
    public BookmarkTeleportHandler(KnowledgeBranch<?, JournalBookmark> branch) {
        super(branch);
    }

    @Override
    public void process() {
        BlockPos target = value.getBlockPos();
        ResourceLocation dimension  = value.getDimension();
        tryTeleport(dimension, target, true, false);
    }
}

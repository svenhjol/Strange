package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.runes.RuneBranch;

public class BookmarkTeleportHandler extends TeleportHandler<Bookmark> {
    public BookmarkTeleportHandler(RuneBranch<?, Bookmark> branch) {
        super(branch);
    }

    @Override
    public void process() {
        BlockPos target = value.getBlockPos();
        ResourceLocation dimension  = value.getDimension();
        tryTeleport(dimension, target, true, false);
    }
}

package svenhjol.strange.module.bookmarks.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.bookmarks.Bookmarks;
import svenhjol.strange.network.Id;
import svenhjol.strange.network.ServerSender;

/**
 * Send all bookmarks to a player client when they connect.
 */
@Id("strange:bookmarks")
public class ServerSendBookmarks extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        var bookmarks = Bookmarks.getBookmarks().orElse(null);
        if (bookmarks == null) return;

        CompoundTag tag = new CompoundTag();
        bookmarks.branch.save(tag);

        send(player, buf -> buf.writeNbt(tag));
    }
}

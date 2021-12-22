package svenhjol.strange.module.bookmarks.network;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.network.Id;
import svenhjol.strange.network.ServerSender;

@Id("strange:new_bookmark")
public class ServerSendNewBookmark extends ServerSender {
    public void send(ServerPlayer player, Bookmark bookmark) {
        send(player, buf -> buf.writeNbt(bookmark.save()));
    }
}

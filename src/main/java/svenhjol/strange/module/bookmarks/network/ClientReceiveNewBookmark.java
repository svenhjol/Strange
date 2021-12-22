package svenhjol.strange.module.bookmarks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.network.ClientReceiver;
import svenhjol.strange.network.Id;

import java.util.Optional;

@Id("strange:new_bookmark")
public class ClientReceiveNewBookmark extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = Optional.ofNullable(buffer.readNbt()).orElseThrow();
        var branch = Optional.ofNullable(BookmarksClient.branch).orElseThrow();

        client.execute(() -> {
            var bookmark = Bookmark.load(tag);
            branch.add(bookmark.getRunes(), bookmark);
            LogHelper.debug(getClass(), "Received new bookmark `" + bookmark.getName() + "` from server.");
        });
    }
}

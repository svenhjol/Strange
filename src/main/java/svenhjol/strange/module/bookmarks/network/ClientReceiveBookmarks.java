package svenhjol.strange.module.bookmarks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.bookmarks.BookmarkBranch;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.network.ClientReceiver;
import svenhjol.strange.network.Id;

import java.util.Optional;

@Id("strange:bookmarks")
public class ClientReceiveBookmarks extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = Optional.ofNullable(buffer.readNbt()).orElseThrow();

        client.execute(() -> {
            BookmarksClient.branch = BookmarkBranch.load(tag);
            LogHelper.debug(getClass(), "Received " + BookmarksClient.branch.size() + " bookmarks from server.");
        });
    }
}

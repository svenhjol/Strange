package svenhjol.strange.module.bookmarks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.bookmarks.BookmarkBranch;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.network.ClientReceiver;
import svenhjol.strange.network.Id;

/**
 * Client receives all bookmarks in the form of a bookmark branch.
 * Deserialize this branch to the client in order to maintain a local copy.
 */
@Id("strange:bookmarks")
public class ClientReceiveBookmarks extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            var branch = BookmarkBranch.load(tag);
            BookmarksClient.setBranch(branch);
            LogHelper.debug(getClass(), "Bookmarks branch has " + branch.size() + " bookmarks.");
        });
    }
}

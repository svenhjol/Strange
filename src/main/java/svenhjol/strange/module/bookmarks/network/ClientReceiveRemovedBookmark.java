package svenhjol.strange.module.bookmarks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.Strange;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarksScreen;

/**
 * Client receives a request to remove a bookmark.
 * Remove the local copy of the bookmark.
 */
@Id("strange:removed_bookmark")
public class ClientReceiveRemovedBookmark extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var branch = BookmarksClient.getBranch().orElseThrow();
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            var bookmark = Bookmark.load(tag);
            branch.remove(bookmark.getRunes());
            LogHelper.debug(Strange.MOD_ID, getClass(), "Removed bookmark is `" + bookmark.getName() + "`.");

            // If the current player is the one who deleted the bookmark then open all bookmarks screen.
            if (client.player == null) return;
            if (client.player.getUUID().equals(bookmark.getUuid())) {
                client.setScreen(new JournalBookmarksScreen());
            }
        });
    }
}

package svenhjol.strange.module.bookmarks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.Strange;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarkScreen;

/**
 * Client receives a newly created bookmark.
 */
@Id("strange:created_bookmark")
public class ClientReceiveCreatedBookmark extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();
        var branch = BookmarksClient.getBranch().orElseThrow();

        client.execute(() -> {
            var bookmark = Bookmark.load(tag);
            branch.add(bookmark.getRunes(), bookmark);
            LogHelper.debug(Strange.MOD_ID, getClass(), "New bookmark name is `" + bookmark.getName() + "`.");

            // If the current player is the one who created the bookmark then open the journal's bookmarks page.
            if (client.player == null) return;
            if (client.player.getUUID().equals(bookmark.getUuid())) {
                client.setScreen(new JournalBookmarkScreen(bookmark));
            }
        });
    }
}

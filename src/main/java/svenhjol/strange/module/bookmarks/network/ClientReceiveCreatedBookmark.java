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
        var startOpened = buffer.readBoolean();
        var branch = BookmarksClient.getBranch().orElseThrow();

        client.execute(() -> {
            var bookmark = Bookmark.load(tag);
            var runes = bookmark.getRunes();
            branch.add(runes, bookmark);
            LogHelper.debug(Strange.MOD_ID, getClass(), "New bookmark name is `" + bookmark.getName() + "`.");

            if (client.player == null) return;

            // If server instructs client to automatically open the bookmark, set screen here.
            if (startOpened && client.player.getUUID().equals(bookmark.getUuid())) {
                client.setScreen(new JournalBookmarkScreen(bookmark));
            }
        });
    }
}

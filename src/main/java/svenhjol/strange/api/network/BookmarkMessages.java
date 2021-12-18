package svenhjol.strange.api.network;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public class BookmarkMessages {
    // Called by client to ask the server to send all bookmarks.
    public static final ResourceLocation SERVER_SYNC_BOOKMARKS = new ResourceLocation(Strange.MOD_ID, "server_sync_bookmarks");

    // Called by server to send all the server-side bookmarks to a client.
    public static final ResourceLocation CLIENT_SYNC_BOOKMARKS = new ResourceLocation(Strange.MOD_ID, "client_sync_bookmarks");
}

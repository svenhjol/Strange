package svenhjol.strange.api.network;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public class BookmarkMessages {
    // Client asks the server to send all bookmarks.
    public static final ResourceLocation SERVER_SYNC_BOOKMARKS = new ResourceLocation(Strange.MOD_ID, "server_sync_bookmarks");

    // Client asks the server to create a new empty bookmark.
    public static final ResourceLocation SERVER_ADD_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "server_add_bookmark2");

    // Client tells the server that a bookmark has been updated.
    public static final ResourceLocation SERVER_UPDATE_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "server_update_bookmark2");

    // Client tells the server that a bookmark has been removed.
    public static final ResourceLocation SERVER_REMOVE_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "server_remove_bookmark2");

    // Server sends all the server-side bookmarks to a client.
    public static final ResourceLocation CLIENT_SYNC_BOOKMARKS = new ResourceLocation(Strange.MOD_ID, "client_sync_bookmarks");

    // Server tells clients that a new bookmark has been created and should be added to each client's local copy.
    public static final ResourceLocation CLIENT_ADD_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "client_add_bookmark2");

    // Server tells clients that a bookmark has been updated and each client's local copy should be updated.
    public static final ResourceLocation CLIENT_UPDATE_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "client_update_bookmark2");

    // Server tells clients that a bookmark has been removed and each client's local copy should also be removed.
    public static final ResourceLocation CLIENT_REMOVE_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "client_remove_bookmark2");
}

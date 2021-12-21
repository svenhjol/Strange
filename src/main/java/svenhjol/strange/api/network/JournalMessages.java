package svenhjol.strange.api.network;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public class JournalMessages {
    // Client asks the server to send the player's journal data.
    public static final ResourceLocation SERVER_SYNC_JOURNAL = new ResourceLocation(Strange.MOD_ID, "server_sync_journal");

    // Client asks the server to make a map for the bookmark.
    public static final ResourceLocation SERVER_MAKE_MAP = new ResourceLocation(Strange.MOD_ID, "server_make_map");

    // Server sends all server-side journal data to a client.
    public static final ResourceLocation CLIENT_SYNC_JOURNAL = new ResourceLocation(Strange.MOD_ID, "client_sync_journal");

    // Server tells client to open the journal on a specific page.
    public static final ResourceLocation CLIENT_OPEN_PAGE = new ResourceLocation(Strange.MOD_ID, "client_open_page");

    // Server sends all bookmark icons to the client.
    public static final ResourceLocation CLIENT_SYNC_BOOKMARK_ICONS = new ResourceLocation(Strange.MOD_ID, "client_sync_bookmark_icons");
}

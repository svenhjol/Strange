package svenhjol.strange.api.network;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public class DiscoveryMessages {
    // Client asks the server to send all discoveries.
    public static final ResourceLocation SERVER_SYNC_DISCOVERIES = new ResourceLocation(Strange.MOD_ID, "server_sync_discoveries");

    // Server sends all the server-side discoveries to a client.
    public static final ResourceLocation CLIENT_SYNC_DISCOVERIES = new ResourceLocation(Strange.MOD_ID, "client_sync_discoveries");

    // Server tells clients that a new discovery has been created and should be added to each client's local copy.
    public static final ResourceLocation CLIENT_ADD_DISCOVERY = new ResourceLocation(Strange.MOD_ID, "client_add_discovery");

    // Server sends discovery data to the client according to the block they interacted with.
    public static final ResourceLocation CLIENT_INTERACT_DISCOVERY = new ResourceLocation(Strange.MOD_ID, "client_interact_discovery");
}

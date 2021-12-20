package svenhjol.strange.api.network;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public class DiscoveryMessages {
    // Client asks the server to send all discoveries.
    public static final ResourceLocation SERVER_SYNC_DISCOVERIES = new ResourceLocation(Strange.MOD_ID, "server_sync_discoveries");

    // Client asks the server to create a new empty discovery.
    public static final ResourceLocation SERVER_ADD_DISCOVERY = new ResourceLocation(Strange.MOD_ID, "server_add_discovery");

    // Client tells the server that a discovery has been updated.
    public static final ResourceLocation SERVER_UPDATE_DISCOVERY = new ResourceLocation(Strange.MOD_ID, "server_update_discovery");

    // Client tells the server that a discovery has been removed.
    public static final ResourceLocation SERVER_REMOVE_DISCOVERY = new ResourceLocation(Strange.MOD_ID, "server_remove_discovery");

    // Server sends all the server-side discoveries to a client.
    public static final ResourceLocation CLIENT_SYNC_DISCOVERIES = new ResourceLocation(Strange.MOD_ID, "client_sync_discoveries");

    // Server tells clients that a new discovery has been created and should be added to each client's local copy.
    public static final ResourceLocation CLIENT_ADD_DISCOVERY = new ResourceLocation(Strange.MOD_ID, "client_add_discovery");

    // Server tells clients that a discovery has been updated and each client's local copy should be updated.
    public static final ResourceLocation CLIENT_UPDATE_DISCOVERY = new ResourceLocation(Strange.MOD_ID, "client_update_discovery");

    // Server tells clients that a discovery has been removed and each client's local copy should also be removed.
    public static final ResourceLocation CLIENT_REMOVE_DISCOVERY = new ResourceLocation(Strange.MOD_ID, "client_remove_discovery");

    // Server sends discovery data to the client according to the block they interacted with.
    public static final ResourceLocation CLIENT_INTERACT_DISCOVERY = new ResourceLocation(Strange.MOD_ID, "client_interact_discovery");
}

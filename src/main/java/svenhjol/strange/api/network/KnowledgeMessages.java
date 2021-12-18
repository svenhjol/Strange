package svenhjol.strange.api.network;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public class KnowledgeMessages {
    public static final ResourceLocation SERVER_SYNC_BIOMES = new ResourceLocation(Strange.MOD_ID, "server_sync_biomes");
    public static final ResourceLocation SERVER_SYNC_DIMENSIONS = new ResourceLocation(Strange.MOD_ID, "server_sync_dimensions");
    public static final ResourceLocation SERVER_SYNC_STRUCTURES = new ResourceLocation(Strange.MOD_ID, "server_sync_structures");
    public static final ResourceLocation SERVER_SYNC_SEED = new ResourceLocation(Strange.MOD_ID, "server_sync_seed");

    public static final ResourceLocation CLIENT_SYNC_BIOMES = new ResourceLocation(Strange.MOD_ID, "client_sync_biomes");
    public static final ResourceLocation CLIENT_SYNC_DIMENSIONS = new ResourceLocation(Strange.MOD_ID, "client_sync_dimensions");
    public static final ResourceLocation CLIENT_SYNC_STRUCTURES = new ResourceLocation(Strange.MOD_ID, "client_sync_structures");
    public static final ResourceLocation CLIENT_SYNC_SEED = new ResourceLocation(Strange.MOD_ID, "client_sync_seed");
}

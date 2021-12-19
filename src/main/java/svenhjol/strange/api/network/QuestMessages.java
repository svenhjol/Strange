package svenhjol.strange.api.network;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public class QuestMessages {
    public static final ResourceLocation SERVER_SYNC_QUESTS = new ResourceLocation(Strange.MOD_ID, "server_sync_quests");
    public static final ResourceLocation SERVER_ABANDON_QUEST = new ResourceLocation(Strange.MOD_ID, "server_abandon_quest");
    public static final ResourceLocation SERVER_PAUSE_QUEST = new ResourceLocation(Strange.MOD_ID, "server_pause_quest");
    public static final ResourceLocation CLIENT_SHOW_QUEST_TOAST = new ResourceLocation(Strange.MOD_ID, "client_show_quest_toast");
    public static final ResourceLocation CLIENT_SYNC_QUESTS = new ResourceLocation(Strange.MOD_ID, "client_sync_quests");
}

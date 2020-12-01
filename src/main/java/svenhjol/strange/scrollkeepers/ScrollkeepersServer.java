package svenhjol.strange.scrollkeepers;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import svenhjol.strange.scrolls.QuestManager;
import svenhjol.strange.scrolls.Scrolls;
import svenhjol.strange.scrolls.tag.Quest;

import static svenhjol.strange.scrollkeepers.Scrollkeepers.MSG_CLIENT_RECEIVE_SCROLL_QUEST;
import static svenhjol.strange.scrollkeepers.Scrollkeepers.MSG_SERVER_GET_SCROLL_QUEST;

public class ScrollkeepersServer {
    public void init() {

        // listen for quest satisfied request coming from the client
        ServerSidePacketRegistry.INSTANCE.register(MSG_SERVER_GET_SCROLL_QUEST, this::handleGetScrollQuest);
    }

    public static void sendScrollQuestPacket(ServerPlayerEntity player, Quest quest) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeCompoundTag(quest.toTag());
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, MSG_CLIENT_RECEIVE_SCROLL_QUEST, buffer);
    }

    private void handleGetScrollQuest(PacketContext context, PacketByteBuf data) {
        String questId = data.readString(4);
        context.getTaskQueue().execute(() -> {
            ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
            if (player == null)
                return;

            if (!Scrolls.getQuestManager().isPresent())
                return;

            QuestManager questManager = Scrolls.getQuestManager().get();
            if (!questManager.getQuest(questId).isPresent())
                return;

            Quest quest = questManager.getQuest(questId).get();
            sendScrollQuestPacket(player, quest);
        });
    }
}

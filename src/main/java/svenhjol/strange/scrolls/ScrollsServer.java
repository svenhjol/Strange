package svenhjol.strange.scrolls;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.List;
import java.util.Optional;

import static svenhjol.strange.scrolls.Scrolls.MSG_CLIENT_CACHE_CURRENT_QUESTS;
import static svenhjol.strange.scrolls.Scrolls.MSG_SERVER_FETCH_CURRENT_QUESTS;

public class ScrollsServer {
    public void init() {
        // handle incoming client packets
        ServerSidePacketRegistry.INSTANCE.register(MSG_SERVER_FETCH_CURRENT_QUESTS, this::handleServerFetchCurrentQuests);
    }

    public static void sendCurrentQuestsPacket(ServerPlayerEntity player) {
        Optional<QuestManager> questManager = Scrolls.getQuestManager();
        questManager.ifPresent(manager -> {
            List<Quest> quests = manager.getQuests(player);

            // convert to nbt and write to packet buffer
            ListTag listTag = new ListTag();
            for (Quest quest : quests) {
                listTag.add(quest.toTag());
            }
            CompoundTag outTag = new CompoundTag();
            outTag.put("quests", listTag);

            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            buffer.writeCompoundTag(outTag);

            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, MSG_CLIENT_CACHE_CURRENT_QUESTS, buffer);
        });
    }

    private void handleServerFetchCurrentQuests(PacketContext context, PacketByteBuf data) {
        context.getTaskQueue().execute(() -> {
            ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
            if (player == null)
                return;

            sendCurrentQuestsPacket(player);
        });
    }
}

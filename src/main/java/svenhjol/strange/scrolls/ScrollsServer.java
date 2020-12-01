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
import java.util.function.BiConsumer;


public class ScrollsServer {
    public void init() {
        // handle incoming client packets
        ServerSidePacketRegistry.INSTANCE.register(Scrolls.MSG_SERVER_OPEN_SCROLL, this::handleServerOpenScroll);
        ServerSidePacketRegistry.INSTANCE.register(Scrolls.MSG_SERVER_FETCH_CURRENT_QUESTS, this::handleServerFetchCurrentQuests);
        ServerSidePacketRegistry.INSTANCE.register(Scrolls.MSG_SERVER_ABANDON_QUEST, this::handleServerAbandonQuest);
    }

    public static void sendPlayerQuestsPacket(ServerPlayerEntity player) {
        Optional<QuestManager> questManager = Scrolls.getQuestManager();
        questManager.ifPresent(manager -> {
            sendQuestsPacket(player, manager.getQuests(player));
        });
    }

    public static void sendPlayerOpenScrollPacket(ServerPlayerEntity player, Quest quest) {
        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeCompoundTag(quest.toTag());
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Scrolls.MSG_CLIENT_OPEN_SCROLL, data);
    }

    public static void sendQuestsPacket(ServerPlayerEntity player, List<Quest> quests) {
        // convert to nbt and write to packet buffer
        ListTag listTag = new ListTag();
        for (Quest quest : quests) {
            listTag.add(quest.toTag());
        }
        CompoundTag outTag = new CompoundTag();
        outTag.put("quests", listTag);

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeCompoundTag(outTag);

        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Scrolls.MSG_CLIENT_CACHE_CURRENT_QUESTS, buffer);
    }

    private void handleServerOpenScroll(PacketContext context, PacketByteBuf data) {
        String questId = data.readString(16);
        if (questId == null || questId.isEmpty())
            return;

        processClientPacket(context, (player, manager) -> {
            Optional<Quest> optionalQuest = manager.getQuest(questId);
            if (!optionalQuest.isPresent())
                return;

            sendPlayerOpenScrollPacket(player, optionalQuest.get());
        });
    }

    private void handleServerFetchCurrentQuests(PacketContext context, PacketByteBuf data) {
        processClientPacket(context, (player, manager) -> {
            sendPlayerQuestsPacket(player);
        });
    }

    private void handleServerAbandonQuest(PacketContext context, PacketByteBuf data) {
        String questId = data.readString(16);
        if (questId == null || questId.isEmpty())
            return;

        processClientPacket(context, (player, manager) -> {
            Optional<Quest> optionalQuest = manager.getQuest(questId);
            if (!optionalQuest.isPresent())
                return;

            Quest quest = optionalQuest.get();
            quest.abandon(player);

            sendPlayerQuestsPacket(player);
        });
    }

    private void processClientPacket(PacketContext context, BiConsumer<ServerPlayerEntity, QuestManager> callback) {
        context.getTaskQueue().execute(() -> {
            ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
            if (player == null)
                return;

            Optional<QuestManager> questManager = Scrolls.getQuestManager();
            questManager.ifPresent(manager -> callback.accept(player, manager));
        });
    }
}

package svenhjol.strange.feature.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charmony.annotation.Packet;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.enums.PacketDirection;
import svenhjol.charmony.iface.IClientNetwork;
import svenhjol.charmony.iface.ICommonRegistry;
import svenhjol.charmony.iface.IPacketRequest;
import svenhjol.charmony.iface.IServerNetwork;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class QuestsNetwork {
    public static void register(ICommonRegistry registry) {
        registry.packet(new SyncQuests(), () -> QuestsClient::handleSyncQuests);
    }

    @Packet(
        id = "strange:request_villager_quests",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "An empty packet sent from the client to request the server to send all quests for a specific villager."
    )
    public static class RequestVillagerQuests implements IPacketRequest {
        private RequestVillagerQuests() {}

        public static void send() {
            clientSender().send(new RequestVillagerQuests());
        }
    }

    @Packet(
        id = "strange:sync_quests",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Send quests to the client."
    )
    public static class SyncQuests implements IPacketRequest {
        static final String QUESTS_TAG = "quests";

        private List<Quest<?>> quests = new ArrayList<>();

        private SyncQuests() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            var tag = new CompoundTag();
            var list = new ListTag();
            for (Quest<?> quest : quests) {
                var q = new CompoundTag();
                quest.save(q);
                list.add(q);
            }
            tag.put(QUESTS_TAG, list);
            buf.writeNbt(tag);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            quests.clear();
            var tag = buf.readNbt();
            if (tag == null) return;

            var list = tag.getList(QUESTS_TAG, 10);
            for (Tag t : list) {
                var quest = Quest.load((CompoundTag) t);
                quests.add(quest);
            }
        }

        public List<Quest<?>> getQuests() {
            return quests;
        }

        public static void send(ServerPlayer player, List<Quest<?>> quests) {
            var message = new SyncQuests();
            message.quests = quests;

            serverSender().send(message, player);
        }
    }

    static IServerNetwork serverSender() {
        return Mods.common(Strange.ID).network();
    }

    static IClientNetwork clientSender() {
        return Mods.client(Strange.ID).network();
    }
}

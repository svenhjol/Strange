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
import svenhjol.strange.feature.quests.Quests.VillagerQuestsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestsNetwork {
    public static void register(ICommonRegistry registry) {
        registry.packet(new NotifyVillagerQuestsResult(), () -> QuestsClient::handleNotifyVillagerQuestsResult);
        registry.packet(new SyncPlayerQuests(), () -> QuestsClient::handleSyncPlayerQuests);
        registry.packet(new SyncVillagerQuests(), () -> QuestsClient::handleSyncVillagerQuests);
        registry.packet(new RequestVillagerQuests(), () -> Quests::handleRequestVillagerQuests);
    }

    @Packet(
        id = "strange:request_villager_quests",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "Sent from the client to request the server to send all quests for a specific villager."
    )
    public static class RequestVillagerQuests implements IPacketRequest {
        private UUID villagerUuid;
        private RequestVillagerQuests() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeUUID(villagerUuid);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            villagerUuid = buf.readUUID();
        }

        public UUID getVillagerUuid() {
            return villagerUuid;
        }

        public static void send(UUID villagerUuid) {
            var message = new RequestVillagerQuests();
            message.villagerUuid = villagerUuid;
            clientSender().send(message);
        }
    }

    @Packet(
        id = "strange:sync_villager_quests",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Send villager quests to the client."
    )
    public static class SyncVillagerQuests extends SyncQuests {
        protected UUID villagerUuid;

        public static void send(ServerPlayer player, List<Quest<?>> quests, UUID villagerUuid) {
            var message = new SyncVillagerQuests();
            message.quests = quests;
            message.villagerUuid = villagerUuid;

            serverSender().send(message, player);
        }

        public UUID getVillagerUuid() {
            return villagerUuid;
        }
    }

    @Packet(
        id = "strange:sync_player_quests",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Send player quests to the client."
    )
    public static class SyncPlayerQuests extends SyncQuests {
        public static void send(ServerPlayer player, List<Quest<?>> quests) {
            var message = new SyncPlayerQuests();
            message.quests = quests;

            serverSender().send(message, player);
        }
    }

    private static abstract class SyncQuests implements IPacketRequest {
        static final String QUESTS_TAG = "quests";

        protected List<Quest<?>> quests = new ArrayList<>();

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
    }

    @Packet(
        id = "strange:notify_villager_quests_result",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Notify the client about the result of the villager quests request."
    )
    public static class NotifyVillagerQuestsResult implements IPacketRequest {
        private VillagerQuestsResult result;

        private NotifyVillagerQuestsResult() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(result);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            this.result = buf.readEnum(VillagerQuestsResult.class);
        }

        public VillagerQuestsResult getResult() {
            return result;
        }

        public static void send(ServerPlayer player, VillagerQuestsResult result) {
            var message = new NotifyVillagerQuestsResult();
            message.result = result;
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

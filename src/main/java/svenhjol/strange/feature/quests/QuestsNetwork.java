package svenhjol.strange.feature.quests;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.VillagerProfession;
import svenhjol.charmony.annotation.Packet;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.enums.PacketDirection;
import svenhjol.charmony.iface.IClientNetwork;
import svenhjol.charmony.iface.ICommonRegistry;
import svenhjol.charmony.iface.IPacketRequest;
import svenhjol.charmony.iface.IServerNetwork;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.Quests.AbandonQuestResult;
import svenhjol.strange.feature.quests.Quests.AcceptQuestResult;
import svenhjol.strange.feature.quests.Quests.VillagerQuestsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestsNetwork {
    public static void register(ICommonRegistry registry) {
        registry.packet(new NotifyVillagerQuestsResult(), () -> QuestsClient::handleNotifyVillagerQuestsResult);
        registry.packet(new NotifyAcceptQuestResult(), () -> QuestsClient::handleAcceptQuestResult);
        registry.packet(new NotifyAbandonQuestResult(), () -> QuestsClient::handleAbandonQuestResult);
        registry.packet(new SyncPlayerQuests(), () -> QuestsClient::handleSyncPlayerQuests);
        registry.packet(new SyncVillagerQuests(), () -> QuestsClient::handleSyncVillagerQuests);
        registry.packet(new RequestVillagerQuests(), () -> Quests::handleRequestVillagerQuests);
        registry.packet(new RequestPlayerQuests(), () -> Quests::handleRequestPlayerQuests);
        registry.packet(new AcceptQuest(), () -> Quests::handleAcceptQuest);
        registry.packet(new AbandonQuest(), () -> Quests::handleAbandonQuest);
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
        id = "strange:request_player_quests",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "Empty packet sent from the client to request the server to send all quests for the player."
    )
    public static class RequestPlayerQuests implements IPacketRequest {
        private RequestPlayerQuests() {}

        public static void send() {
            clientSender().send(new RequestPlayerQuests());
        }
    }

    @Packet(
        id = "strange:accept_quest",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "Quest ID and villager UUID sent from client to server when player accepts the quest."
    )
    public static class AcceptQuest implements IPacketRequest {
        private UUID villagerUuid;
        private String questId;

        private AcceptQuest() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeUUID(villagerUuid);
            buf.writeUtf(questId);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            villagerUuid = buf.readUUID();
            questId = buf.readUtf();
        }

        public UUID getVillagerUuid() {
            return villagerUuid;
        }

        public String getQuestId() {
            return questId;
        }

        public static void send(UUID villagerUuid, String questId) {
            var message = new AcceptQuest();
            message.villagerUuid = villagerUuid;
            message.questId = questId;
            clientSender().send(message);
        }
    }

    @Packet(
        id = "strange:abandon_quest",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "Quest ID sent from client to server when player abandons the quest."
    )
    public static class AbandonQuest implements IPacketRequest {
        private String questId;

        private AbandonQuest() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(questId);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            questId = buf.readUtf();
        }

        public String getQuestId() {
            return questId;
        }

        public static void send(String questId) {
            var message = new AbandonQuest();
            message.questId = questId;
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
        protected VillagerProfession profession;

        @Override
        public void encode(FriendlyByteBuf buf) {
            super.encode(buf);
            buf.writeUUID(villagerUuid);
            buf.writeUtf(BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).toString());
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            super.decode(buf);
            villagerUuid = buf.readUUID();
            profession = BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(buf.readUtf()));
        }

        public static void send(ServerPlayer player, List<Quest<?>> quests, UUID villagerUuid, VillagerProfession profession) {
            var message = new SyncVillagerQuests();
            message.quests = quests;
            message.villagerUuid = villagerUuid;
            message.profession = profession;

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

    @Packet(
        id = "strange:notify_accept_quest_result",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Notify the client about the result of accepting the quest."
    )
    public static class NotifyAcceptQuestResult implements IPacketRequest {
        private AcceptQuestResult result;

        private NotifyAcceptQuestResult() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(result);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            this.result = buf.readEnum(AcceptQuestResult.class);
        }

        public AcceptQuestResult getResult() {
            return result;
        }

        public static void send(ServerPlayer player, AcceptQuestResult result) {
            var message = new NotifyAcceptQuestResult();
            message.result = result;
            serverSender().send(message, player);
        }
    }

    @Packet(
        id = "strange:notify_abandon_quest_result",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Notify the client about the result of abandoning the quest."
    )
    public static class NotifyAbandonQuestResult implements IPacketRequest {
        private AbandonQuestResult result;

        private NotifyAbandonQuestResult() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(result);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            this.result = buf.readEnum(AbandonQuestResult.class);
        }

        public AbandonQuestResult getResult() {
            return result;
        }

        public static void send(ServerPlayer player, AbandonQuestResult result) {
            var message = new NotifyAbandonQuestResult();
            message.result = result;
            serverSender().send(message, player);
        }
    }

    /**
     * Abstraction for syncing quests for villagers/players.
     */
    static abstract class SyncQuests implements IPacketRequest {
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

    static IServerNetwork serverSender() {
        return Mods.common(Strange.ID).network();
    }

    static IClientNetwork clientSender() {
        return Mods.client(Strange.ID).network();
    }
}

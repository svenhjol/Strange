package svenhjol.strange.feature.learned_runes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charmony.annotation.Packet;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.enums.PacketDirection;
import svenhjol.charmony.iface.ICommonRegistry;
import svenhjol.charmony.iface.IPacketRequest;
import svenhjol.charmony.iface.IServerNetwork;
import svenhjol.strange.Strange;

public class LearnedRunesNetwork {
    public static void register(ICommonRegistry registry) {
        registry.packet(new SyncLearned(), () -> LearnedRunesClient::handleSyncLearned);
    }

    static IServerNetwork serverSender() {
        return Mods.common(Strange.ID).network();
    }

    @Packet(
        id = "strange:sync_learned",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Synchronise all learned items to the client."
    )
    public static class SyncLearned implements IPacketRequest {
        private LearnedList learned;

        private SyncLearned() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            var tag = learned.save();
            buf.writeNbt(tag);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            var tag = buf.readNbt();
            if (tag != null) {
                learned = LearnedList.load(tag);
            } else {
                throw new RuntimeException("Server did not sync learned data");
            }
        }

        public LearnedList getLearned() {
            return learned;
        }

        public static void send(ServerPlayer player, LearnedList learned) {
            var message = new SyncLearned();
            message.learned = learned;
            serverSender().send(message, player);
        }
    }
}

package svenhjol.strange.feature.travel_journal;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charmony.annotation.Packet;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.enums.PacketDirection;
import svenhjol.charmony.iface.ICommonRegistry;
import svenhjol.charmony.iface.IPacketRequest;
import svenhjol.strange.Strange;

public class TravelJournalNetwork {
    public static void register(ICommonRegistry registry) {
        registry.packet(new SentTravelJournalLearned(), () -> TravelJournalClient::handleSentLearned);
    }

    @Packet(
        id = "strange:sent_travel_journal_learned",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Synchronise all learned items with the client."
    )
    public static class SentTravelJournalLearned implements IPacketRequest {
        private Learned learned;

        private SentTravelJournalLearned() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            var tag = learned.save();
            buf.writeNbt(tag);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            var tag = buf.readNbt();
            if (tag != null) {
                learned = Learned.load(tag);
            } else {
                throw new RuntimeException("Server did not sync the travel journal!");
            }
        }

        public Learned getLearned() {
            return learned;
        }

        public static void send(ServerPlayer player, Learned learned) {
            var message = new SentTravelJournalLearned();
            message.learned = learned;

            Mods.common(Strange.ID).network().send(message, player);
        }
    }
}

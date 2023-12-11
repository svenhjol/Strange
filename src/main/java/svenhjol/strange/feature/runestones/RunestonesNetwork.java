package svenhjol.strange.feature.runestones;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charmony.annotation.Packet;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.enums.PacketDirection;
import svenhjol.charmony.iface.ICommonRegistry;
import svenhjol.charmony.iface.IPacketRequest;
import svenhjol.strange.Strange;

public class RunestonesNetwork {
    public static void register(ICommonRegistry registry) {
        registry.packet(new SentLevelSeed(), () -> RunestonesClient::handleSendLevelSeed);
    }

    @Packet(
        id = "strange:sent_level_seed",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Send the level seed to the client."
    )
    public static class SentLevelSeed implements IPacketRequest {
        private long seed;

        private SentLevelSeed() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeLong(seed);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            seed = buf.readLong();
        }

        public long getSeed() {
            return seed;
        }

        public static void send(ServerPlayer player, long seed) {
            var message = new SentLevelSeed();
            message.seed = seed;

            Mods.common(Strange.ID).network().send(message, player);
        }
    }
}

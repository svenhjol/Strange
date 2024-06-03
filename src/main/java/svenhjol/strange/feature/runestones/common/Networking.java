package svenhjol.strange.feature.runestones.common;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.runestones.Runestones;

public final class Networking extends FeatureHolder<Runestones> {
    public Networking(Runestones feature) {
        super(feature);
    }

    // Server-to-client packet that contains the current world seed.
    public record S2CWorldSeed(long seed) implements CustomPacketPayload {
        public static Type<S2CWorldSeed> TYPE = new Type<>(Strange.id("world_seed"));
        public static StreamCodec<FriendlyByteBuf, S2CWorldSeed> CODEC =
            StreamCodec.of(S2CWorldSeed::encode, S2CWorldSeed::decode);

        public static void send(ServerPlayer player, long seed) {
            ServerPlayNetworking.send(player, new S2CWorldSeed(seed));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CWorldSeed self) {
            buf.writeLong(self.seed());
        }

        private static S2CWorldSeed decode(FriendlyByteBuf buf) {
            return new S2CWorldSeed(buf.readLong());
        }
    }
}

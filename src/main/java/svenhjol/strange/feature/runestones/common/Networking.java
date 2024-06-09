package svenhjol.strange.feature.runestones.common;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.runestones.Runestones;

public final class Networking extends FeatureHolder<Runestones> {
    public Networking(Runestones feature) {
        super(feature);
    }

    public record C2SLookingAtRunestone(BlockPos pos) implements CustomPacketPayload {
        public static Type<C2SLookingAtRunestone> TYPE = new Type<>(Strange.id("looking_at_runestone"));
        public static StreamCodec<FriendlyByteBuf, C2SLookingAtRunestone> CODEC =
            StreamCodec.of(C2SLookingAtRunestone::encode, C2SLookingAtRunestone::decode);

        public static void send(BlockPos pos) {
            ClientPlayNetworking.send(new C2SLookingAtRunestone(pos));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, C2SLookingAtRunestone self) {
            buf.writeBlockPos(self.pos());
        }

        private static C2SLookingAtRunestone decode(FriendlyByteBuf buf) {
            return new C2SLookingAtRunestone(buf.readBlockPos());
        }
    }

    // Server-to-client packet that contains the current world seed.
    public record S2CWorldSeed(long seed) implements CustomPacketPayload {
        public static Type<S2CWorldSeed> TYPE = new Type<>(Strange.id("world_seed_for_runestones"));
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

    // Server-to-client packet that informs the client that a runestone wants to consume an item.
    public record S2CSacrificeInProgress(BlockPos runestonePos, Vec3 itemPos) implements CustomPacketPayload {
        public static Type<S2CSacrificeInProgress> TYPE = new Type<>(Strange.id("sacrifice_in_progress"));
        public static StreamCodec<FriendlyByteBuf, S2CSacrificeInProgress> CODEC =
            StreamCodec.of(S2CSacrificeInProgress::encode, S2CSacrificeInProgress::decode);

        public static void send(ServerPlayer player, BlockPos runestonePos, Vec3 itemPos) {
            ServerPlayNetworking.send(player, new S2CSacrificeInProgress(runestonePos, itemPos));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CSacrificeInProgress self) {
            buf.writeBlockPos(self.runestonePos);
            buf.writeVec3(self.itemPos);
        }

        private static S2CSacrificeInProgress decode(FriendlyByteBuf buf) {
            return new S2CSacrificeInProgress(buf.readBlockPos(), buf.readVec3());
        }
    }

    public record S2CActivateRunestone(BlockPos pos) implements CustomPacketPayload{
        public static Type<S2CActivateRunestone> TYPE = new Type<>(Strange.id("activate_runestone"));
        public static StreamCodec<FriendlyByteBuf, S2CActivateRunestone> CODEC =
            StreamCodec.of(S2CActivateRunestone::encode, S2CActivateRunestone::decode);

        public static void send(ServerPlayer player, BlockPos pos) {
            ServerPlayNetworking.send(player, new S2CActivateRunestone(pos));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CActivateRunestone self) {
            buf.writeBlockPos(self.pos);
        }

        private static S2CActivateRunestone decode(FriendlyByteBuf buf) {
            return new S2CActivateRunestone(buf.readBlockPos());
        }
    }
}

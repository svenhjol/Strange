package svenhjol.strange.feature.travel_journals.common;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.travel_journals.TravelJournals;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public final class Networking extends FeatureHolder<TravelJournals> {
    public Networking(TravelJournals feature) {
        super(feature);
    }

    public record S2CTakePhoto(UUID uuid) implements CustomPacketPayload {
        public static final Type<S2CTakePhoto> TYPE = new Type<>(Strange.id("take_photo"));
        public static final StreamCodec<FriendlyByteBuf, S2CTakePhoto> CODEC =
            StreamCodec.of(S2CTakePhoto::encode, S2CTakePhoto::decode);

        public static void send(ServerPlayer player, UUID uuid) {
            ServerPlayNetworking.send(player, new S2CTakePhoto(uuid));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CTakePhoto self) {
            buf.writeUUID(self.uuid());
        }

        private static S2CTakePhoto decode(FriendlyByteBuf buf) {
            return new S2CTakePhoto(buf.readUUID());
        }
    }
    
    public record S2CPhoto(UUID uuid, BufferedImage image) implements CustomPacketPayload {
        public static final Type<S2CPhoto> TYPE = new Type<>(Strange.id("server_photo"));
        public static final StreamCodec<FriendlyByteBuf, S2CPhoto> CODEC =
            StreamCodec.of(S2CPhoto::encode, S2CPhoto::decode);

        public static void send(ServerPlayer player, UUID uuid, BufferedImage image) {
            ServerPlayNetworking.send(player, new S2CPhoto(uuid, image));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CPhoto self) {
            var stream = new ByteArrayOutputStream();
            try {
                ImageIO.write(self.image(), "png", stream);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write image to stream: " + e.getMessage());
            }

            buf.writeUUID(self.uuid());
            buf.writeByteArray(stream.toByteArray());
        }

        private static S2CPhoto decode(FriendlyByteBuf buf) {
            var uuid = buf.readUUID();
            var bytes = buf.readByteArray();

            BufferedImage image;
            try {
                image = ImageIO.read(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read image from stream: " + e.getMessage());
            }

            return new S2CPhoto(uuid, image);
        }
    }

    public record C2SMakeBookmark(String name) implements CustomPacketPayload {
        public static final Type<C2SMakeBookmark> TYPE = new Type<>(Strange.id("make_bookmark"));
        public static final StreamCodec<FriendlyByteBuf, C2SMakeBookmark> CODEC =
            StreamCodec.of(C2SMakeBookmark::encode, C2SMakeBookmark::decode);

        public static void send(String name) {
            ClientPlayNetworking.send(new C2SMakeBookmark(name));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, C2SMakeBookmark self) {
            buf.writeUtf(self.name());
        }

        private static C2SMakeBookmark decode(FriendlyByteBuf buf) {
            return new C2SMakeBookmark(buf.readUtf());
        }
    }

    public record C2SPhoto(UUID uuid, BufferedImage image) implements CustomPacketPayload {
        public static final Type<C2SPhoto> TYPE = new Type<>(Strange.id("client_photo"));
        public static final StreamCodec<FriendlyByteBuf, C2SPhoto> CODEC =
            StreamCodec.of(C2SPhoto::encode, C2SPhoto::decode);

        public static void send(UUID uuid, BufferedImage image) {
            ClientPlayNetworking.send(new C2SPhoto(uuid, image));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, C2SPhoto self) {
            var stream = new ByteArrayOutputStream();
            try {
                ImageIO.write(self.image(), "png", stream);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write image to stream: " + e.getMessage());
            }

            buf.writeUUID(self.uuid());
            buf.writeByteArray(stream.toByteArray());
        }

        private static C2SPhoto decode(FriendlyByteBuf buf) {
            var uuid = buf.readUUID();
            var bytes = buf.readByteArray();

            BufferedImage image;
            try {
                image = ImageIO.read(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read image from stream: " + e.getMessage());
            }

            return new C2SPhoto(uuid, image);
        }
    }
    
    // Client-to-server packet to request the server to download the photo that matches the UUID.
    public record C2SDownloadPhoto(UUID uuid) implements CustomPacketPayload {
        public static final Type<C2SDownloadPhoto> TYPE = new Type<>(Strange.id("download_photo"));
        public static final StreamCodec<FriendlyByteBuf, C2SDownloadPhoto> CODEC =
            StreamCodec.of(C2SDownloadPhoto::encode, C2SDownloadPhoto::decode);

        public static void send(UUID uuid) {
            ClientPlayNetworking.send(new C2SDownloadPhoto(uuid));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, C2SDownloadPhoto self) {
            buf.writeUUID(self.uuid());
        }

        private static C2SDownloadPhoto decode(FriendlyByteBuf buf) {
            return new C2SDownloadPhoto(buf.readUUID());
        }
    }
}
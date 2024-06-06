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

    public record C2SSendPhoto(UUID uuid, BufferedImage image) implements CustomPacketPayload {
        public static final Type<C2SSendPhoto> TYPE = new Type<>(Strange.id("send_photo"));
        public static final StreamCodec<FriendlyByteBuf, C2SSendPhoto> CODEC =
            StreamCodec.of(C2SSendPhoto::encode, C2SSendPhoto::decode);

        public static void send(UUID uuid, BufferedImage image) {
            ClientPlayNetworking.send(new C2SSendPhoto(uuid, image));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, C2SSendPhoto self) {
            var stream = new ByteArrayOutputStream();
            try {
                ImageIO.write(self.image(), "png", stream);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write image to stream: " + e.getMessage());
            }

            buf.writeUUID(self.uuid());
            buf.writeByteArray(stream.toByteArray());
        }

        private static C2SSendPhoto decode(FriendlyByteBuf buf) {
            var uuid = buf.readUUID();
            var bytes = buf.readByteArray();

            BufferedImage image;
            try {
                image = ImageIO.read(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read image from stream: " + e.getMessage());
            }

            return new C2SSendPhoto(uuid, image);
        }
    }
}
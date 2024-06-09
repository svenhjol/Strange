package svenhjol.strange.feature.travel_journals.common;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
    
    /**
     * Server-to-client packet to tell the client to prepare a photo for the given journal ID and bookmark ID.
     */
    public record S2CTakePhoto(UUID journalId, UUID bookmarkId) implements CustomPacketPayload {
        public static final Type<S2CTakePhoto> TYPE = new Type<>(Strange.id("take_photo"));
        public static final StreamCodec<FriendlyByteBuf, S2CTakePhoto> CODEC =
            StreamCodec.of(S2CTakePhoto::encode, S2CTakePhoto::decode);

        public static void send(ServerPlayer player, UUID journalId, UUID bookmarkId) {
            ServerPlayNetworking.send(player, new S2CTakePhoto(journalId, bookmarkId));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CTakePhoto self) {
            buf.writeUUID(self.journalId());
            buf.writeUUID(self.bookmarkId());
        }

        private static S2CTakePhoto decode(FriendlyByteBuf buf) {
            return new S2CTakePhoto(buf.readUUID(), buf.readUUID());
        }
    }

    /**
     * Server-to-client packet to send image data for the given bookmark ID.
     */
    public record S2CPhoto(UUID bookmarkId, BufferedImage image) implements CustomPacketPayload {
        public static final Type<S2CPhoto> TYPE = new Type<>(Strange.id("server_photo"));
        public static final StreamCodec<FriendlyByteBuf, S2CPhoto> CODEC =
            StreamCodec.of(S2CPhoto::encode, S2CPhoto::decode);

        public static void send(ServerPlayer player, UUID bookmarkId, BufferedImage image) {
            ServerPlayNetworking.send(player, new S2CPhoto(bookmarkId, image));
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

            buf.writeUUID(self.bookmarkId());
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

    /**
     * Client-to-server packet to request the server create a bookmark in the closest available journal using the given name.
     */
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

    /**
     * Client-to-server packet to request the server updates a bookmark using the given journal and bookmark data.
     */
    public record C2SUpdateBookmark(UUID journalId, BookmarkData bookmark) implements CustomPacketPayload {
        public static final Type<C2SUpdateBookmark> TYPE = new Type<>(Strange.id("update_bookmark"));
        public static final StreamCodec<RegistryFriendlyByteBuf, C2SUpdateBookmark> CODEC =
            StreamCodec.of(C2SUpdateBookmark::encode, C2SUpdateBookmark::decode);

        public static void send(UUID journalId, BookmarkData bookmark) {
            ClientPlayNetworking.send(new C2SUpdateBookmark(journalId, bookmark));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buf, C2SUpdateBookmark self) {
            buf.writeUUID(self.journalId());
            BookmarkData.STREAM_CODEC.encode(buf, self.bookmark());
        }

        private static C2SUpdateBookmark decode(RegistryFriendlyByteBuf buf) {
            var journalId = buf.readUUID();
            var bookmark = BookmarkData.STREAM_CODEC.decode(buf);
            return new C2SUpdateBookmark(journalId, bookmark);
        }
    }

    /**
     * Client-to-server packet to request the server deletes a bookmark using the given journal and bookmark IDs.
     */
    public record C2SDeleteBookmark(UUID journalId, UUID bookmarkId) implements CustomPacketPayload {
        public static final Type<C2SDeleteBookmark> TYPE = new Type<>(Strange.id("delete_bookmark"));
        public static final StreamCodec<RegistryFriendlyByteBuf, C2SDeleteBookmark> CODEC =
            StreamCodec.of(C2SDeleteBookmark::encode, C2SDeleteBookmark::decode);

        public static void send(UUID journalId, UUID bookmarkId) {
            ClientPlayNetworking.send(new C2SDeleteBookmark(journalId, bookmarkId));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buf, C2SDeleteBookmark self) {
            buf.writeUUID(self.journalId());
            buf.writeUUID(self.bookmarkId());
        }

        private static C2SDeleteBookmark decode(RegistryFriendlyByteBuf buf) {
            var journalId = buf.readUUID();
            var bookmarkId = buf.readUUID();
            return new C2SDeleteBookmark(journalId, bookmarkId);
        }
    }

    /**
     * Client-to-server packet to request the server exports a map using the given bookmark.
     */
    public record C2SExportMap(BookmarkData bookmark) implements CustomPacketPayload {
        public static final Type<C2SExportMap> TYPE = new Type<>(Strange.id("export_map"));
        public static final StreamCodec<RegistryFriendlyByteBuf, C2SExportMap> CODEC =
            StreamCodec.of(C2SExportMap::encode, C2SExportMap::decode);

        public static void send(BookmarkData bookmark) {
            ClientPlayNetworking.send(new C2SExportMap(bookmark));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buf, C2SExportMap self) {
            BookmarkData.STREAM_CODEC.encode(buf, self.bookmark());
        }

        private static C2SExportMap decode(RegistryFriendlyByteBuf buf) {
            var bookmark = BookmarkData.STREAM_CODEC.decode(buf);
            return new C2SExportMap(bookmark);
        }
    }
    
    /**
     * Client-to-server packet to request the server exports a page using the given bookmark.
     */
    public record C2SExportPage(BookmarkData bookmark) implements CustomPacketPayload {
        public static final Type<C2SExportPage> TYPE = new Type<>(Strange.id("export_page"));
        public static final StreamCodec<RegistryFriendlyByteBuf, C2SExportPage> CODEC =
            StreamCodec.of(C2SExportPage::encode, C2SExportPage::decode);

        public static void send(BookmarkData bookmark) {
            ClientPlayNetworking.send(new C2SExportPage(bookmark));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buf, C2SExportPage self) {
            BookmarkData.STREAM_CODEC.encode(buf, self.bookmark());
        }

        private static C2SExportPage decode(RegistryFriendlyByteBuf buf) {
            var bookmark = BookmarkData.STREAM_CODEC.decode(buf);
            return new C2SExportPage(bookmark);
        }
    }

    /**
     * Client-to-server packet to send image data for the given bookmark ID.
     */
    public record C2SPhoto(UUID bookmarkId, BufferedImage image) implements CustomPacketPayload {
        public static final Type<C2SPhoto> TYPE = new Type<>(Strange.id("client_photo"));
        public static final StreamCodec<FriendlyByteBuf, C2SPhoto> CODEC =
            StreamCodec.of(C2SPhoto::encode, C2SPhoto::decode);

        public static void send(UUID bookmarkId, BufferedImage image) {
            ClientPlayNetworking.send(new C2SPhoto(bookmarkId, image));
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

            buf.writeUUID(self.bookmarkId());
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

    /**
     * Client-to-server packet to request download of the photo that matches the UUID.
     */
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
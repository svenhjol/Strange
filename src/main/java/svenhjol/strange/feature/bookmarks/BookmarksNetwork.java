package svenhjol.strange.feature.bookmarks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import svenhjol.charmony.annotation.Packet;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.enums.PacketDirection;
import svenhjol.charmony.iface.IClientNetwork;
import svenhjol.charmony.iface.ICommonRegistry;
import svenhjol.charmony.iface.IPacketRequest;
import svenhjol.charmony.iface.IServerNetwork;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class BookmarksNetwork {
    public static void register(ICommonRegistry registry) {
        registry.packet(new SyncBookmarks(), () -> BookmarksClient::handleSyncBookmarks);
        registry.packet(new NotifyAddBookmarkResult(), () -> BookmarksClient::handleNotifyNewBookmarkResult);
        registry.packet(new NotifyUpdateBookmarkResult(), () -> BookmarksClient::handleNotifyUpdateBookmarkResult);
        registry.packet(new NotifyDeleteBookmarkResult(), () -> BookmarksClient::handleNotifyDeleteBookmarkResult);
        registry.packet(new SendNewBookmark(), () -> BookmarksClient::handleNewBookmark);
        registry.packet(new SendChangedBookmark(), () -> BookmarksClient::handleChangedBookmark);
        registry.packet(new SendItemIcons(), () -> BookmarksClient::handleSendItemIcons);
        registry.packet(new RequestChangeBookmark(), () -> Bookmarks::handleRequestChangeBookmark);
        registry.packet(new RequestDeleteBookmark(), () -> Bookmarks::handleRequestDeleteBookmark);
        registry.packet(new RequestNewBookmark(), () -> Bookmarks::handleRequestNewBookmark);
        registry.packet(new RequestItemIcons(), () -> Bookmarks::handleRequestItemIcons);
    }

    static IServerNetwork serverSender() {
        return Mods.common(Strange.ID).network();
    }

    static IClientNetwork clientSender() {
        return Mods.client(Strange.ID).network();
    }

    @Packet(
        id = "strange:sync_bookmarks",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Synchronise all bookmarks to the client."
    )
    public static class SyncBookmarks implements IPacketRequest {
        private BookmarkList bookmarks;

        private SyncBookmarks() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            var tag = bookmarks.save();
            buf.writeNbt(tag);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            var tag = buf.readNbt();
            if (tag != null) {
                bookmarks = BookmarkList.load(tag);
            } else {
                throw new RuntimeException("Server did not sync bookmarks data");
            }
        }

        public BookmarkList getBookmarks() {
            return bookmarks;
        }

        public static void send(ServerPlayer player, BookmarkList bookmarks) {
            var message = new SyncBookmarks();
            message.bookmarks = bookmarks;
            serverSender().send(message, player);
        }
    }

    @Packet(
        id = "strange:notify_add_bookmark_result",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Notify the client about the result of the bookmark creation."
    )
    public static class NotifyAddBookmarkResult implements IPacketRequest {
        private BookmarkList.AddBookmarkResult result;

        private NotifyAddBookmarkResult() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(result);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            this.result = buf.readEnum(BookmarkList.AddBookmarkResult.class);
        }

        public BookmarkList.AddBookmarkResult getResult() {
            return result;
        }

        public static void send(ServerPlayer player, BookmarkList.AddBookmarkResult result) {
            var message = new NotifyAddBookmarkResult();
            message.result = result;
            serverSender().send(message, player);
        }
    }

    @Packet(
        id = "strange:notify_update_bookmark_result",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Notify the client about the result of the bookmark update."
    )
    public static class NotifyUpdateBookmarkResult implements IPacketRequest {
        private BookmarkList.UpdateBookmarkResult result;

        private NotifyUpdateBookmarkResult() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(result);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            this.result = buf.readEnum(BookmarkList.UpdateBookmarkResult.class);
        }

        public BookmarkList.UpdateBookmarkResult getResult() {
            return result;
        }

        public static void send(ServerPlayer player, BookmarkList.UpdateBookmarkResult result) {
            var message = new NotifyUpdateBookmarkResult();
            message.result = result;
            serverSender().send(message, player);
        }
    }

    @Packet(
        id = "strange:notify_delete_bookmark_result",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Notify the client about the result of the bookmark delete."
    )
    public static class NotifyDeleteBookmarkResult implements IPacketRequest {
        private BookmarkList.DeleteBookmarkResult result;

        private NotifyDeleteBookmarkResult() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(result);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            this.result = buf.readEnum(BookmarkList.DeleteBookmarkResult.class);
        }

        public BookmarkList.DeleteBookmarkResult getResult() {
            return result;
        }

        public static void send(ServerPlayer player, BookmarkList.DeleteBookmarkResult result) {
            var message = new NotifyDeleteBookmarkResult();
            message.result = result;
            serverSender().send(message, player);
        }
    }

    @Packet(
        id = "strange:request_change_bookmark",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "A bookmark changed on the client that should be updated on the server."
    )
    public static class RequestChangeBookmark implements IPacketRequest {
        private Bookmark bookmark;

        private RequestChangeBookmark() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeNbt(bookmark.save());
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            var tag = buf.readNbt();
            if (tag != null) {
                this.bookmark = Bookmark.load(tag);
            }
        }

        public Bookmark getBookmark() {
            return bookmark;
        }

        public static void send(Bookmark bookmark) {
            var message = new RequestChangeBookmark();
            message.bookmark = bookmark;
            clientSender().send(message);
        }
    }

    @Packet(
        id = "strange:request_delete_bookmark",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "A bookmark deleted on the client that should be updated on the server."
    )
    public static class RequestDeleteBookmark implements IPacketRequest {
        private Bookmark bookmark;

        private RequestDeleteBookmark() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeNbt(bookmark.save());
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            var tag = buf.readNbt();
            if (tag != null) {
                this.bookmark = Bookmark.load(tag);
            }
        }

        public Bookmark getBookmark() {
            return bookmark;
        }

        public static void send(Bookmark bookmark) {
            var message = new RequestDeleteBookmark();
            message.bookmark = bookmark;
            clientSender().send(message);
        }
    }

    @Packet(
        id = "strange:request_new_bookmark",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "An empty packet sent from the client to request the server to make a new bookmark."
    )
    public static class RequestNewBookmark implements IPacketRequest {
        private RequestNewBookmark() {}

        public static void send() {
            clientSender().send(new RequestNewBookmark());
        }
    }

    @Packet(
        id = "strange:request_item_icons",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "An empty packet sent from the client to request the server to send all item icons."
    )
    public static class RequestItemIcons implements IPacketRequest {
        private RequestItemIcons() {}

        public static void send() {
            clientSender().send(new RequestItemIcons());
        }
    }

    @Packet(
        id = "strange:send_new_bookmark",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "The new bookmark created on the server, sent to the client."
    )
    public static class SendNewBookmark implements IPacketRequest {
        private Bookmark bookmark;
        private SendNewBookmark() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeNbt(bookmark.save());
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            var tag = buf.readNbt();
            if (tag != null) {
                this.bookmark = Bookmark.load(tag);
            }
        }

        public Bookmark getBookmark() {
            return bookmark;
        }

        public static void send(ServerPlayer player, Bookmark bookmark) {
            var message = new SendNewBookmark();
            message.bookmark = bookmark;
            serverSender().send(message, player);
        }
    }

    @Packet(
        id = "strange:send_changed_bookmark",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "The changed bookmark updated on the server, sent to the client."
    )
    public static class SendChangedBookmark implements IPacketRequest {
        private Bookmark bookmark;
        private SendChangedBookmark() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeNbt(bookmark.save());
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            var tag = buf.readNbt();
            if (tag != null) {
                this.bookmark = Bookmark.load(tag);
            }
        }

        public Bookmark getBookmark() {
            return bookmark;
        }

        public static void send(ServerPlayer player, Bookmark bookmark) {
            var message = new SendChangedBookmark();
            message.bookmark = bookmark;
            serverSender().send(message, player);
        }
    }

    @Packet(
        id = "strange:item_icons",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Item icons read from the server and sent to the client."
    )
    public static class SendItemIcons implements IPacketRequest {
        private List<ResourceLocation> icons;

        private SendItemIcons() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            var tag = new CompoundTag();
            var listTag = new ListTag();
            icons.forEach(
                icon -> listTag.add(StringTag.valueOf(icon.toString())));

            tag.put("list", listTag);
            buf.writeNbt(tag);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            var tag = buf.readNbt();
            icons = new ArrayList<>();

            if (tag != null) {
                var listTag = tag.getList("list", 8);
                listTag.stream().map(Tag::getAsString)
                    .forEach(t -> icons.add(new ResourceLocation(t)));
            }
        }

        public List<Item> getIcons() {
            return icons.stream().map(BuiltInRegistries.ITEM::get).toList();
        }

        public static void send(ServerPlayer player, List<Item> icons) {
            var message = new SendItemIcons();
            message.icons = icons.stream().map(BuiltInRegistries.ITEM::getKey).toList();
            serverSender().send(message, player);
        }
    }
}

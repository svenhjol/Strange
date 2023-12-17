package svenhjol.strange.feature.travel_journal;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charmony.annotation.Packet;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.enums.PacketDirection;
import svenhjol.charmony.iface.ICommonRegistry;
import svenhjol.charmony.iface.IPacketRequest;
import svenhjol.charmony.iface.IServerNetwork;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.travel_journal.Bookmarks.AddBookmarkResult;

public class TravelJournalNetwork {
    public static void register(ICommonRegistry registry) {
        registry.packet(new SyncLearned(), () -> TravelJournalClient::handleSyncLearned);
        registry.packet(new SyncBookmarks(), () -> TravelJournalClient::handleSyncBookmarks);
        registry.packet(new NotifyNewBookmarkResult(), () -> TravelJournalClient::handleNotifyNewBookmarkResult);
        registry.packet(new MakeNewBookmark(), () -> TravelJournal::handleMakeNewBookmark);
    }

    static IServerNetwork serverNetwork() {
        return Mods.common(Strange.ID).network();
    }

    @Packet(
        id = "strange:sync_learned",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Synchronise all learned items to the client."
    )
    public static class SyncLearned implements IPacketRequest {
        private Learned learned;

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
                learned = Learned.load(tag);
            } else {
                throw new RuntimeException("Server did not sync learned data");
            }
        }

        public Learned getLearned() {
            return learned;
        }

        public static void send(ServerPlayer player, Learned learned) {
            var message = new SyncLearned();
            message.learned = learned;
            serverNetwork().send(message, player);
        }
    }

    @Packet(
        id = "strange:sync_bookmarks",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Synchronise all bookmarks to the client."
    )
    public static class SyncBookmarks implements IPacketRequest {
        private Bookmarks bookmarks;

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
                bookmarks = Bookmarks.load(tag);
            } else {
                throw new RuntimeException("Server did not sync bookmarks data");
            }
        }

        public Bookmarks getBookmarks() {
            return bookmarks;
        }

        public static void send(ServerPlayer player, Bookmarks bookmarks) {
            var message = new SyncBookmarks();
            message.bookmarks = bookmarks;
            serverNetwork().send(message, player);
        }
    }

    @Packet(
        id = "strange:notify_new_bookmark_result",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Notify the client about the result of the bookmark creation."
    )
    public static class NotifyNewBookmarkResult implements IPacketRequest {
        private AddBookmarkResult result;

        private NotifyNewBookmarkResult() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(result);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            this.result = buf.readEnum(AddBookmarkResult.class);
        }

        public AddBookmarkResult getResult() {
            return result;
        }

        public static void send(ServerPlayer player, AddBookmarkResult result) {
            var message = new NotifyNewBookmarkResult();
            message.result = result;
            serverNetwork().send(message, player);
        }
    }

    @Packet(
        id = "strange:make_new_bookmark",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "An empty packet sent from the client to instruct the server to make a new bookmark."
    )
    public static class MakeNewBookmark implements IPacketRequest {
        private MakeNewBookmark() {}

        public static void send() {
            Mods.client(Strange.ID).network().send(new MakeNewBookmark());
        }
    }
}

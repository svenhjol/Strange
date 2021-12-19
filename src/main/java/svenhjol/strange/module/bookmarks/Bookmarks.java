package svenhjol.strange.module.bookmarks;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.network.BookmarkMessages;
import svenhjol.strange.module.bookmarks.exception.BookmarkException;

import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID)
public class Bookmarks extends CharmModule {
    private static BookmarkData bookmarkData;

    public static int maxBookmarksPerPlayer = 50;

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayNetworking.registerGlobalReceiver(BookmarkMessages.SERVER_SYNC_BOOKMARKS, this::handleSyncBookmarks);
        ServerPlayNetworking.registerGlobalReceiver(BookmarkMessages.SERVER_ADD_BOOKMARK, this::handleAddBookmark);
        ServerPlayNetworking.registerGlobalReceiver(BookmarkMessages.SERVER_UPDATE_BOOKMARK, this::handleUpdateBookmark);
        ServerPlayNetworking.registerGlobalReceiver(BookmarkMessages.SERVER_REMOVE_BOOKMARK, this::handleRemoveBookmark);
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {

        // Overworld is loaded first. We set up the bookmarks storage at this point.
        if (level.dimension() == Level.OVERWORLD) {
            ServerLevel overworld = (ServerLevel) level;
            DimensionDataStorage storage = overworld.getDataStorage();

            bookmarkData = storage.computeIfAbsent(
                tag -> BookmarkData.load(overworld, tag),
                () -> new BookmarkData(overworld),
                BookmarkData.getFileId(level.dimensionType())
            );
        }
    }

    private void handleSyncBookmarks(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var bookmarks = getBookmarks().orElseThrow();

        server.execute(() -> {
            CompoundTag tag = new CompoundTag();
            bookmarks.bookmarks.save(tag);
            NetworkHelper.sendPacketToClient(player, BookmarkMessages.CLIENT_SYNC_BOOKMARKS, buf -> buf.writeNbt(tag));
        });
    }

    private void handleAddBookmark(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var bookmarks = getBookmarks().orElseThrow();

        server.execute(() -> {
            try {
                var bookmark = bookmarks.add(player);
                NetworkHelper.sendPacketToAllClients(server, BookmarkMessages.CLIENT_ADD_BOOKMARK, buf -> buf.writeNbt(bookmark.save()));
            } catch (BookmarkException e) {
                LogHelper.warn(getClass(), "Failed to register bookmark: " + e.getMessage());
            }
        });
    }

    private void handleUpdateBookmark(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var bookmarks = getBookmarks().orElseThrow();
        var tag = Optional.ofNullable(buffer.readNbt()).orElseThrow();

        server.execute(() -> {
            var bookmark = Bookmark.load(tag);

            try {
                var updated = bookmarks.update(bookmark);
                NetworkHelper.sendPacketToAllClients(server, BookmarkMessages.CLIENT_UPDATE_BOOKMARK, buf -> buf.writeNbt(updated.save()));
            } catch (BookmarkException e) {
                LogHelper.warn(getClass(), "Failed to update bookmark: " + e.getMessage());
            }
        });
    }

    private void handleRemoveBookmark(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var bookmarks = getBookmarks().orElseThrow();
        var tag = Optional.ofNullable(buffer.readNbt()).orElseThrow();

        server.execute(() -> {
            var bookmark = Bookmark.load(tag);

            try {
                bookmarks.remove(bookmark);
                NetworkHelper.sendPacketToAllClients(server, BookmarkMessages.CLIENT_REMOVE_BOOKMARK, buf -> buf.writeNbt(bookmark.save()));
            } catch (BookmarkException e) {
                LogHelper.warn(getClass(), "Failed to remove bookmark: " + e.getMessage());
            }
        });
    }

    public static Optional<BookmarkData> getBookmarks() {
        return Optional.ofNullable(bookmarkData);
    }
}
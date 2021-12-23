package svenhjol.strange.module.bookmarks;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.bookmarks.network.*;

import javax.annotation.Nullable;
import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true, description = "Reads and writes player bookmarks.")
public class Bookmarks extends CharmModule {
    private static @Nullable BookmarkData bookmarkData;

    public static ServerSendBookmarks SEND_BOOKMARKS;
    public static ServerSendCreatedBookmark SEND_CREATED_BOOKMARK;
    public static ServerSendRemovedBookmark SEND_REMOVED_BOOKMARK;
    public static ServerSendUpdatedBookmark SEND_UPDATED_BOOKMARK;
    public static ServerReceiveCreateBookmark RECEIVE_CREATE_BOOKMARK;
    public static ServerReceiveRemoveBookmark RECEIVE_REMOVE_BOOKMARK;
    public static ServerReceiveUpdateBookmark RECEIVE_UPDATE_BOOKMARK;

    public static int maxBookmarksPerPlayer = 50;

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayConnectionEvents.JOIN.register(this::handlePlayerJoin);

        SEND_BOOKMARKS = new ServerSendBookmarks();
        SEND_CREATED_BOOKMARK = new ServerSendCreatedBookmark();
        SEND_REMOVED_BOOKMARK = new ServerSendRemovedBookmark();
        SEND_UPDATED_BOOKMARK = new ServerSendUpdatedBookmark();
        RECEIVE_CREATE_BOOKMARK = new ServerReceiveCreateBookmark();
        RECEIVE_REMOVE_BOOKMARK = new ServerReceiveRemoveBookmark();
        RECEIVE_UPDATE_BOOKMARK = new ServerReceiveUpdateBookmark();
    }

    public static Optional<BookmarkData> getBookmarks() {
        return Optional.ofNullable(bookmarkData);
    }

    private void handlePlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        SEND_BOOKMARKS.send(listener.getPlayer());
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        // Overworld is loaded first.
        // We set up the bookmarks storage at this point.
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
}
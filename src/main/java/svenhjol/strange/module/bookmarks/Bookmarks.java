package svenhjol.strange.module.bookmarks;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.api.event.PlayerDieCallback;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.bookmarks.network.*;

import javax.annotation.Nullable;
import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID, priority = 10, description = "Catalogues player bookmarks.")
public class Bookmarks extends CharmModule {
    private static @Nullable BookmarkData bookmarkData;

    public static ServerSendBookmarks SEND_BOOKMARKS;
    public static ServerSendCreatedBookmark SEND_CREATED_BOOKMARK;
    public static ServerSendRemovedBookmark SEND_REMOVED_BOOKMARK;
    public static ServerSendUpdatedBookmark SEND_UPDATED_BOOKMARK;
    public static ServerReceiveCreateBookmark RECEIVE_CREATE_BOOKMARK;
    public static ServerReceiveRemoveBookmark RECEIVE_REMOVE_BOOKMARK;
    public static ServerReceiveUpdateBookmark RECEIVE_UPDATE_BOOKMARK;

    @Config(name = "Maximum bookmarks", description = "The maximum number of bookmarks each player can create.")
    public static int maxBookmarksPerPlayer = 50;

    @Config(name = "Death bookmark", description = "If true, a bookmark marking the player's death location will automatically be created.")
    public static boolean createDeathBookmark = true;

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayConnectionEvents.JOIN.register(this::handlePlayerJoin);
        PlayerDieCallback.EVENT.register(this::handlePlayerDie);

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

    private void handlePlayerDie(ServerPlayer player, DamageSource damageSource) {
        if (!createDeathBookmark) return;

        var bookmarks = Bookmarks.getBookmarks().orElse(null);
        if (bookmarks == null) return;

        var bookmark = bookmarks.addDeath(player);
        Bookmarks.SEND_CREATED_BOOKMARK.sendToAll(player.level.getServer(), bookmark, false);
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
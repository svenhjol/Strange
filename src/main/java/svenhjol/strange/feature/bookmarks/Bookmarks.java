package svenhjol.strange.feature.bookmarks;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.helper.TagHelper;
import svenhjol.strange.StrangeTags;
import svenhjol.strange.feature.bookmarks.BookmarksNetwork.*;
import svenhjol.strange.feature.travel_journal.TravelJournal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class Bookmarks extends CommonFeature {
    public static final String BOOKMARKS_TAG = "bookmarks";
    public static final Map<UUID, BookmarkList> BOOKMARKS = new HashMap<>();
    public static Supplier<SoundEvent> photoSound;
    public static boolean showCoordinates = true;

    @Override
    public void register() {
        var registry = mod().registry();
        BookmarksNetwork.register(registry);

        photoSound = registry.soundEvent("travel_journal_photo");
    }

    @Override
    public void runWhenEnabled() {
        TravelJournal.registerPlayerDataSource(
            (player, tag) -> BOOKMARKS.put(player.getUUID(), BookmarkList.load(tag.getCompound(BOOKMARKS_TAG))),
            (player, tag) -> tag.put(BOOKMARKS_TAG, getBookmarks(player).save()));

        TravelJournal.registerSyncHandler(Bookmarks::syncBookmarks);
    }

    public static BookmarkList getBookmarks(Player player) {
        return BOOKMARKS.getOrDefault(player.getUUID(), new BookmarkList());
    }

    public static void syncBookmarks(ServerPlayer player) {
        SyncBookmarks.send(player, getBookmarks(player));
    }

    public static void handleRequestNewBookmark(RequestNewBookmark message, Player player) {
        var bookmarks = getBookmarks(player);
        var serverPlayer = (ServerPlayer)player;
        var newBookmark = Bookmark.playerDefault(player);
        var result = bookmarks.add(newBookmark);

        // Always update the client with the result of the bookmark creation.
        NotifyAddBookmarkResult.send(serverPlayer, result);

        if (result == BookmarkList.AddBookmarkResult.SUCCESS) {
            // Send the new bookmark to the client.
            syncBookmarks(serverPlayer);

            // Send the new bookmark back to the client.
            SendNewBookmark.send(serverPlayer, newBookmark);
        }
    }

    public static void handleRequestChangeBookmark(RequestChangeBookmark message, Player player) {
        var bookmarks = getBookmarks(player);
        var bookmark = message.getBookmark();
        var serverPlayer = (ServerPlayer)player;
        var result = bookmarks.update(bookmark);

        // Always update the client with the result of the bookmark update.
        NotifyUpdateBookmarkResult.send(serverPlayer, result);

        if (result == BookmarkList.UpdateBookmarkResult.SUCCESS) {
            // Sync the new state with the client.
            syncBookmarks(serverPlayer);

            // Send the updated bookmark back to the client.
            SendChangedBookmark.send(serverPlayer, bookmark);
        }
    }

    public static void handleRequestItemIcons(RequestItemIcons message, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            var level = (ServerLevel)serverPlayer.level();
            var itemRegistry = level.registryAccess().registryOrThrow(Registries.ITEM);
            var bookmarkIcons = TagHelper.getValues(itemRegistry, StrangeTags.BOOKMARK_ICONS);
            SendItemIcons.send(serverPlayer, bookmarkIcons);
        }
    }

    public static void handleRequestDeleteBookmark(RequestDeleteBookmark message, Player player) {
        var bookmarks = getBookmarks(player);
        var serverPlayer = (ServerPlayer)player;
        var result = bookmarks.delete(message.getBookmark());

        if (result == BookmarkList.DeleteBookmarkResult.SUCCESS) {
            // Sync the new state with the client.
            syncBookmarks(serverPlayer);
        }

        // Always update the client with the result of the bookmark deletion.
        NotifyDeleteBookmarkResult.send(serverPlayer, result);
    }
}

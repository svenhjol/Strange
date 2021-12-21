package svenhjol.strange.module.bookmarks;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.api.network.BookmarkMessages;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarkScreen;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarksScreen;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ClientModule(module = Bookmarks.class)
public class BookmarksClient extends CharmModule {
    public static @Nullable BookmarkBranch branch;

    @Override
    public void runWhenEnabled() {
        ClientPlayNetworking.registerGlobalReceiver(BookmarkMessages.CLIENT_SYNC_BOOKMARKS, this::handleSyncBookmarks);
        ClientPlayNetworking.registerGlobalReceiver(BookmarkMessages.CLIENT_ADD_BOOKMARK, this::handleAddBookmark);
        ClientPlayNetworking.registerGlobalReceiver(BookmarkMessages.CLIENT_UPDATE_BOOKMARK, this::handleUpdateBookmark);
        ClientPlayNetworking.registerGlobalReceiver(BookmarkMessages.CLIENT_REMOVE_BOOKMARK, this::handleRemoveBookmark);
    }

    private void handleSyncBookmarks(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var tag = buffer.readNbt();
        if (tag == null) return;

        client.execute(() -> {
            branch = BookmarkBranch.load(tag);
            LogHelper.debug(getClass(), "Received " + branch.size() + " bookmarks from server.");
        });
    }

    /**
     * Keeps the local client copy of bookmarks in sync with bookmarks created on the server.
     * If the current player is the one who created the bookmark, open the journal bookmark page so they can view it.
     */
    private void handleAddBookmark(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        processBookmark(client, buffer.readNbt(), (branch, bookmark) -> {

            // Add the new bookmark to the local branch.
            branch.add(bookmark.getRunes(), bookmark);
            LogHelper.debug(getClass(), "Received new bookmark `" + bookmark.getName() + "` from server.");

        }, bookmark -> client.setScreen(new JournalBookmarkScreen(bookmark)));
    }

    /**
     * When a bookmark is updated, the server sends the modified bookmark to all players.
     * If the current player is the one who modified the bookmark, open the journal's bookmarks page.
     */
    private void handleUpdateBookmark(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        processBookmark(client, buffer.readNbt(), (branch, bookmark) -> {

            // We can use add() to update the existing bookmark because the runes remain the same.
            branch.add(bookmark.getRunes(), bookmark);
            LogHelper.debug(getClass(), "Received request to update bookmark with runes `" + bookmark.getRunes() + "` from server.");

        }, bookmark -> client.setScreen(new JournalBookmarksScreen()));
    }

    /**
     * When a bookmark is removed, the server sends the removed bookmark to all players.
     * If the current player is the one who deleted it, open the journal's bookmarks page.
     */
    private void handleRemoveBookmark(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        processBookmark(client, buffer.readNbt(), (branch, bookmark) -> {

            // Remove the local copy.
            branch.remove(bookmark.getRunes());
            LogHelper.debug(getClass(), "Received request to remove bookmark with runes `" + bookmark.getRunes() + "` from server.");

        }, bookmark -> client.setScreen(new JournalBookmarksScreen()));
    }

    /**
     * Convenience method to fetch local bookmarks, unserialize the bookmark from the server, and run a consumer using the branch and bookmark.
     * If the current player created/updated the bookmark then the onCurrentPlayer consumer will be run.
     */
    private void processBookmark(Minecraft client, @Nullable CompoundTag tag, BiConsumer<BookmarkBranch, Bookmark> onBranch, Consumer<Bookmark> onCurrentPlayer) {
        if (client.player == null) return;
        if (tag == null) return;

        var bookmarks = BookmarksClient.branch;
        if (bookmarks == null) return;

        client.execute(() -> {
            // deserialize the bookmark tag and run the consumer
            var bookmark = Bookmark.load(tag);
            onBranch.accept(bookmarks, bookmark);

            // check the current player's uuid against the bookmark's uuid and run the consumer
            if (bookmark.getUuid().equals(client.player.getUUID())) {
                onCurrentPlayer.accept(bookmark);
            }
        });
    }

    /**
     * Convenience method to instruct the server to create a new bookmark for the current player.
     */
    public static void sendAddBookmark() {
        NetworkHelper.sendEmptyPacketToServer(BookmarkMessages.SERVER_ADD_BOOKMARK);
    }

    /**
     * Convenience method to instruct the server to update a bookmark for the current player.
     */
    public static void sendUpdateBookmark(Bookmark bookmark) {
        NetworkHelper.sendPacketToServer(BookmarkMessages.SERVER_UPDATE_BOOKMARK, buf -> buf.writeNbt(bookmark.save()));
    }

    /**
     * Convenience method to instruct the server to delete a bookmark for the current player.
     */
    public static void sendRemoveBookmark(Bookmark bookmark) {
        NetworkHelper.sendPacketToServer(BookmarkMessages.SERVER_REMOVE_BOOKMARK, buf -> buf.writeNbt(bookmark.save()));
    }

    public static ItemStack getBookmarkIconItem(Bookmark bookmark) {
        var icon = bookmark.getIcon();
        return new ItemStack(Registry.ITEM.get(icon));
    }
}

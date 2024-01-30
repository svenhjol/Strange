package svenhjol.strange.feature.bookmarks;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.lwjgl.glfw.GLFW;
import svenhjol.charmony.api.event.ClientTickEvent;
import svenhjol.charmony.api.event.HudRenderEvent;
import svenhjol.charmony.api.event.KeyPressEvent;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.bookmarks.BookmarksNetwork.*;
import svenhjol.strange.feature.bookmarks.client.screen.BookmarkScreen;
import svenhjol.strange.feature.bookmarks.client.BookmarksButtons.BookmarksButton;
import svenhjol.strange.feature.bookmarks.client.BookmarksButtons.BookmarksShortcutButton;
import svenhjol.strange.feature.bookmarks.client.screen.BookmarksScreen;
import svenhjol.strange.feature.travel_journal.TravelJournalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BookmarksClient extends ClientFeature {
    static Supplier<String> newBookmarkKey;
    static long lastBookmarkTimestamp;
    static Photo photo = null;
    public static final List<Item> BOOKMARK_ICONS = new ArrayList<>();

    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return Bookmarks.class;
    }

    @Override
    public void register() {
        var registry = mod().registry();

        newBookmarkKey = registry.key("new_bookmark",
            () -> new KeyMapping("key.strange.new_bookmark", GLFW.GLFW_KEY_B, "key.categories.misc"));
    }

    @Override
    public void runWhenEnabled() {
        KeyPressEvent.INSTANCE.handle(this::handleKeyPress);
        ClientTickEvent.INSTANCE.handle(this::handleClientTick);
        HudRenderEvent.INSTANCE.handle(this::handleHudRender);

        TravelJournalClient.registerShortcut(
            (x, y) -> new BookmarksShortcutButton(x, y, BookmarksClient::openBookmarksScreen));

        TravelJournalClient.registerHomeButton(
            (x, y) -> new BookmarksButton(x - (BookmarksButton.WIDTH / 2), y, BookmarksClient::openBookmarksScreen));
    }

    private void handleKeyPress(String id) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        if (id.equals(newBookmarkKey.get())) {
            if (minecraft.level.getGameTime() - lastBookmarkTimestamp < 10) {
                return;
            }

            lastBookmarkTimestamp = minecraft.level.getGameTime();
            makeNewBookmark();
        }
    }

    private void handleHudRender(GuiGraphics guiGraphics, float tickDelta) {
        if (photo != null && photo.isValid()) {
            photo.renderCountdown(guiGraphics);
        }
    }

    private void handleClientTick(Minecraft minecraft) {
        if (photo != null) {
            if (!photo.isValid()) {
                logDebugMessage("Removing completed photo");
                var bookmark = photo.getBookmark();
                photo = null;

                minecraft.setScreen(new BookmarkScreen(bookmark));
            } else {
                photo.tick();
            }
        }
    }

    public static void handleSyncBookmarks(SyncBookmarks message, Player player) {
        logDebugMessage("Received bookmarks from server");
        Bookmarks.BOOKMARKS.put(player.getUUID(), message.getBookmarks());
    }

    public static void handleNotifyNewBookmarkResult(NotifyAddBookmarkResult message, Player player) {
        var result = message.getResult();
        logDebugMessage("Received add bookmark result " + result);
    }

    public static void handleNotifyUpdateBookmarkResult(NotifyUpdateBookmarkResult message, Player player) {
        var result = message.getResult();
        logDebugMessage("Received update bookmark result " + result);
    }

    public static void handleNotifyDeleteBookmarkResult(NotifyDeleteBookmarkResult message, Player player) {
        var result = message.getResult();
        logDebugMessage("Received update bookmark result " + result);

        if (result == BookmarkList.DeleteBookmarkResult.SUCCESS) {
            // Trigger a page redirect back to all bookmarks.
            Minecraft.getInstance().setScreen(new BookmarksScreen());
        }
    }

    public static void handleNewBookmark(SendNewBookmark message, Player player) {
        var bookmark = message.getBookmark();

        // Now we have the bookmark, take a photo for it.
        initPhoto(bookmark);
    }

    public static void handleChangedBookmark(SendChangedBookmark message, Player player) {
        Minecraft.getInstance().setScreen(new BookmarkScreen(message.getBookmark()));
    }

    public static void handleSendItemIcons(SendItemIcons message, Player player) {
        BOOKMARK_ICONS.clear();
        BOOKMARK_ICONS.addAll(message.getIcons());
    }

    public static void makeNewBookmark() {
        logDebugMessage("Sending new bookmark packet to server");
        RequestNewBookmark.send();
    }

    public static void changeBookmark(Bookmark bookmark) {
        RequestChangeBookmark.send(bookmark);
    }

    public static void initPhoto(Bookmark bookmark) {
        photo = new Photo(bookmark);
        Minecraft.getInstance().setScreen(null);
    }

    public static void openBookmarksScreen(Button button) {
        Minecraft.getInstance().setScreen(new BookmarksScreen());
    }

    public static void openBookmarksScreen(int page) {
        Minecraft.getInstance().setScreen(new BookmarksScreen(page));
    }

    private static void logDebugMessage(String message) {
        Mods.client(Strange.ID).log().debug(TravelJournalClient.class, message);
    }
}

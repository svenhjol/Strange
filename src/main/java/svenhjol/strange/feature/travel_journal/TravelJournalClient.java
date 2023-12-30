package svenhjol.strange.feature.travel_journal;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.lwjgl.glfw.GLFW;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.api.event.ClientTickEvent;
import svenhjol.charmony.api.event.HudRenderEvent;
import svenhjol.charmony.api.event.KeyPressEvent;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.travel_journal.TravelJournalNetwork.*;
import svenhjol.strange.feature.travel_journal.client.BookmarkScreen;
import svenhjol.strange.feature.travel_journal.client.BookmarksScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TravelJournalClient extends ClientFeature {
    static Supplier<String> openJournalKey;
    static Supplier<String> newBookmarkKey;
    static long lastBookmarkTimestamp;
    static Photo photo = null;
    public static final List<Item> BOOKMARK_ICONS = new ArrayList<>();

    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return TravelJournal.class;
    }

    @Override
    public void register() {
        var registry = mod().registry();

        openJournalKey = registry.key("open_journal",
            () -> new KeyMapping("key.strange.open_journal", GLFW.GLFW_KEY_J, "key.categories.misc"));
        newBookmarkKey = registry.key("new_bookmark",
            () -> new KeyMapping("key.strange.new_bookmark", GLFW.GLFW_KEY_B, "key.categories.misc"));
    }

    @Override
    public void runWhenEnabled() {
        KeyPressEvent.INSTANCE.handle(this::handleKeyPress);
        ClientTickEvent.INSTANCE.handle(this::handleClientTick);
        HudRenderEvent.INSTANCE.handle(this::handleHudRender);
    }

    private void handleHudRender(GuiGraphics guiGraphics, float tickDelta) {
        if (photo != null && photo.isValid()) {
            photo.renderCountdown(guiGraphics);
        }
    }

    public static void handleSyncLearned(SyncLearned message, Player player) {
        logDebugMessage("Received learned from server with " + message.getLearned().getLocations().size() + " location(s)");
        TravelJournal.LEARNED.put(player.getUUID(), message.getLearned());
    }

    public static void handleSyncBookmarks(SyncBookmarks message, Player player) {
        logDebugMessage("Received bookmarks from server");
        TravelJournal.BOOKMARKS.put(player.getUUID(), message.getBookmarks());
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

        if (result == Bookmarks.DeleteBookmarkResult.SUCCESS) {
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

    private void handleKeyPress(String id) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        if (id.equals(openJournalKey.get())) {
            openJournal();
        }

        if (id.equals(newBookmarkKey.get())) {
            if (minecraft.level.getGameTime() - lastBookmarkTimestamp < 10) {
                return;
            }

            lastBookmarkTimestamp = minecraft.level.getGameTime();
            makeNewBookmark();
        }
    }

    private void openJournal() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        if (PageTracker.screen == null) {
            PageTracker.Screen.HOME.open();
        } else {
            PageTracker.screen.open();
        }

        if (minecraft.player != null) {
            minecraft.player.playSound(TravelJournal.interactSound.get(), 0.5f, 1.0f);
        }
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

    private static void logDebugMessage(String message) {
        Mods.client(Strange.ID).log().debug(TravelJournalClient.class, message);
    }
}

package svenhjol.strange.feature.travel_journal;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony_api.event.KeyPressEvent;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.travel_journal.TravelJournalNetwork.MakeNewBookmark;
import svenhjol.strange.feature.travel_journal.TravelJournalNetwork.SyncBookmarks;
import svenhjol.strange.feature.travel_journal.TravelJournalNetwork.SyncLearned;
import svenhjol.strange.feature.travel_journal.client.HomeScreen;

import java.util.function.Supplier;

public class TravelJournalClient extends ClientFeature {
    static Supplier<String> openJournalKey;
    static Supplier<String> newBookmarkKey;
    static long lastBookmarkTimestamp;
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
    }

    public static void handleSyncLearned(SyncLearned message, Player player) {
        logDebugMessage("Received learned from server");
        TravelJournal.LEARNED.put(player.getUUID(), message.getLearned());
    }

    public static void handleSyncBookmarks(SyncBookmarks message, Player player) {
        logDebugMessage("Received bookmarks from server");
        TravelJournal.BOOKMARKS.put(player.getUUID(), message.getBookmarks());
    }

    public static void handleNotifyNewBookmarkResult(TravelJournalNetwork.NotifyNewBookmarkResult message, Player player) {
        var result = message.getResult();
        logDebugMessage("Received new bookmark result " + result);
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

        minecraft.setScreen(new HomeScreen());
    }

    private void makeNewBookmark() {
        logDebugMessage("Sending new bookmark packet to server");
        MakeNewBookmark.send();
    }

    private static void logDebugMessage(String message) {
        Mods.client(Strange.ID).log().debug(TravelJournalClient.class, message);
    }
}

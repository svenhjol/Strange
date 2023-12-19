package svenhjol.strange.feature.travel_journal;

import net.minecraft.client.Minecraft;
import svenhjol.strange.feature.travel_journal.client.*;

import java.util.function.Supplier;

public class PageTracker {
    public static Screen screen;
    public static Bookmark bookmark;

    public enum Screen {
        HOME(HomeScreen::new),
        BOOKMARKS(BookmarksScreen::new),
        BOOKMARK(() -> bookmark == null ? new BookmarksScreen() : new BookmarkScreen(bookmark)),
        LEARNED(LearnedScreen::new);

        private final Supplier<BaseScreen> screen;

        Screen(Supplier<BaseScreen> screen) {
            this.screen = screen;
        }

        public void set() {
            PageTracker.screen = this;
        }

        public void open() {
            var minecraft = Minecraft.getInstance();
            minecraft.setScreen(screen.get());
        }
    }
}

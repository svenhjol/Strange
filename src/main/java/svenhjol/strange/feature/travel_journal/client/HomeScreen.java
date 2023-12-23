package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.runestones.Runestones;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;

public class HomeScreen extends BaseScreen {
    public HomeScreen() {
        super(TravelJournalResources.HOME_TITLE);
        PageTracker.Screen.HOME.set();
    }

    @Override
    protected void init() {
        super.init();
        int yoffset = 45;

        addRenderableWidget(new BookmarksButton(midX - (BookmarksButton.WIDTH / 2), yoffset, this::openBookmarks));
        yoffset += 24;

        if (Mods.common(Strange.ID).loader().isEnabled(Runestones.class)) {
            addRenderableWidget(new LearnedButton(midX - (LearnedButton.WIDTH / 2), yoffset, this::openLearned));
            yoffset += 24;
        }

        addRenderableWidget(new CloseButton(midX - (CloseButton.WIDTH / 2),220, b -> onClose()));

        initShortcuts();
    }

    static class LearnedButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.LEARNED_BUTTON_TEXT;

        protected LearnedButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class BookmarksButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.BOOKMARKS_BUTTON_TEXT;

        protected BookmarksButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }
}

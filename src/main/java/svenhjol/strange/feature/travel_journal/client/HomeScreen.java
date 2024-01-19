package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.runestones.Runestones;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;

public class HomeScreen extends BaseTravelJournalScreen {
    public HomeScreen() {
        super(TravelJournalResources.HOME_TITLE);
        PageTracker.Screen.HOME.set();
    }

    @Override
    protected void init() {
        super.init();
        var yOffset = 45;
        var lineHeight = 24;
        var loader = Mods.common(Strange.ID).loader();

        addRenderableWidget(new BookmarksButton(midX - (BookmarksButton.WIDTH / 2), yOffset, this::openBookmarks));
        yOffset += lineHeight;

        if (loader.isEnabled(Runestones.class)) {
            addRenderableWidget(new LearnedButton(midX - (LearnedButton.WIDTH / 2), yOffset, this::openLearned));
            yOffset += lineHeight;
        }

        if (loader.isEnabled(Quests.class)) {
            addRenderableWidget(new QuestsButton(midX - (QuestsButton.WIDTH / 2), yOffset, this::openQuests));
            yOffset += lineHeight;
        }

        addRenderableWidget(new Buttons.CloseButton(midX - (Buttons.CloseButton.WIDTH / 2),220, b -> onClose()));

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

    static class QuestsButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.QUESTS_BUTTON_TEXT;

        protected QuestsButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }
}

package svenhjol.strange.module.journals;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.journals.Journals.Page;
import svenhjol.strange.module.journals.screen.JournalHomeScreen;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarkScreen;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarksScreen;
import svenhjol.strange.module.journals.screen.knowledge.*;
import svenhjol.strange.module.journals.screen.quest.JournalQuestHomeScreen;
import svenhjol.strange.module.journals.screen.quest.JournalQuestsScreen;
import svenhjol.strange.module.quests.Quest;

public class JournalViewer {
    private static Page lastPage;
    private static JournalBookmark lastBookmark;
    private static ResourceLocation lastResource;
    private static Quest lastQuest;
    private static int lastPageOffset;

    public static JournalScreen getScreen(Page page) {
        JournalScreen screen;

        if (page.equals(Page.HOME) && lastPage == null) {

            // we specifically want the home page here
            screen = new JournalHomeScreen();

        } else if (page.equals(Page.HOME)) {

            // if previous page recorded, redirect to it here
            switch (lastPage) {
                case BOOKMARK -> screen = new JournalBookmarkScreen(lastBookmark);
                case QUEST -> screen = new JournalQuestHomeScreen(lastQuest);
                case BIOME -> screen = new JournalBiomeScreen(lastResource);
                case DIMENSION -> screen = new JournalDimensionScreen(lastResource);
                case STRUCTURE -> screen = new JournalStructureScreen(lastResource);
                case BOOKMARKS -> screen = new JournalBookmarksScreen();
                case BIOMES -> screen = new JournalBiomesScreen();
                case DIMENSIONS -> screen = new JournalDimensionsScreen();
                case STRUCTURES -> screen = new JournalStructuresScreen();
                case QUESTS -> screen = new JournalQuestsScreen();
                case KNOWLEDGE -> screen = new JournalKnowledgeScreen();
                default -> screen = new JournalHomeScreen();
            }

            screen.setLastPageOffset(lastPageOffset);

        } else {

            // we want to set the screen to the network packet
            switch (page) {
                case BOOKMARKS -> screen = new JournalBookmarksScreen();
                case QUESTS -> screen = new JournalQuestsScreen();
                case KNOWLEDGE -> screen = new JournalKnowledgeScreen();
                default -> screen = new JournalHomeScreen();
            }
        }

        return screen;
    }

    public static void viewedPage(Page page) {
        viewedPage(page, 0);
    }

    public static void viewedPage(Page page, int pageOffset) {
        lastPage = page;
        lastPageOffset = pageOffset;
    }

    public static void viewedBookmark(JournalBookmark bookmark) {
        lastPage = Page.BOOKMARK;
        lastBookmark = bookmark;
    }

    public static void viewedQuest(Quest quest) {
        lastPage = Page.QUEST;
        lastQuest = quest;
    }

    public static void viewedResource(Page page, ResourceLocation res) {
        lastPage = page;
        lastResource = res;
    }
}

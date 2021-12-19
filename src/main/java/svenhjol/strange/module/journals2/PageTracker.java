package svenhjol.strange.module.journals2;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals.Journals.Page;
import svenhjol.strange.module.journals.screen.JournalHomeScreen;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarkScreen;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarksScreen;
import svenhjol.strange.module.journals.screen.knowledge.*;
import svenhjol.strange.module.journals.screen.quest.JournalQuestHomeScreen;
import svenhjol.strange.module.journals.screen.quest.JournalQuestsScreen;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestData;
import svenhjol.strange.module.quests.QuestsClient;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class PageTracker {
    private Page page;
    private Bookmark bookmark;
    private Quest quest;
    private ResourceLocation resource;
    private int offset;

    public void setPage(Page page) {
        setPage(page, 0);
    }

    public void setPage(Page page, int offset) {
        this.page = page;
        this.offset = offset;
    }

    public void setBookmark(Page page, Bookmark bookmark) {
        this.page = page;
        this.bookmark = bookmark.copy();
        this.offset = 0;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
        this.offset = 0;
    }

    public void setResource(Page page, ResourceLocation resource) {
        this.page = page;
        this.resource = resource;
        this.offset = 0;
    }

    public JournalScreen getScreen() {
        return getScreen(null);
    }

    public JournalScreen getScreen(@Nullable Page wantsPage) {
        JournalScreen screen;

        if (page == null) {

            // we specifically want the home page here
            screen = new JournalHomeScreen();

        } else if (wantsPage == null) {

            // if previous page recorded, redirect to it here
            switch (page) {
                case BOOKMARK -> screen = new JournalBookmarkScreen(bookmark);
                case QUEST -> {
                    Optional<QuestData> quests = QuestsClient.getQuestData();
                    if (quests.isPresent() && quests.get().has(quest.getId())) {
                        screen = new JournalQuestHomeScreen(quest);
                    } else {
                        screen = new JournalQuestsScreen();
                    }
                }
                case BIOME -> screen = new JournalBiomeScreen(resource);
                case DIMENSION -> screen = new JournalDimensionScreen(resource);
                case STRUCTURE -> screen = new JournalStructureScreen(resource);
                case BOOKMARKS -> screen = new JournalBookmarksScreen();
                case BIOMES -> screen = new JournalBiomesScreen();
                case DIMENSIONS -> screen = new JournalDimensionsScreen();
                case STRUCTURES -> screen = new JournalStructuresScreen();
                case QUESTS -> screen = new JournalQuestsScreen();
                case KNOWLEDGE -> screen = new JournalKnowledgeScreen();
                default -> screen = new JournalHomeScreen();
            }

            screen.setOffset(offset);

        } else {

            // we want to set the screen to the network packet
            switch (wantsPage) {
                case BOOKMARKS -> screen = new JournalBookmarksScreen();
                case QUESTS -> screen = new JournalQuestsScreen();
                case KNOWLEDGE -> screen = new JournalKnowledgeScreen();
                default -> screen = new JournalHomeScreen();
            }
        }

        return screen;
    }
}

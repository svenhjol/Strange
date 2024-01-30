package svenhjol.strange.feature.bookmarks.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.bookmarks.Bookmark;
import svenhjol.strange.feature.bookmarks.BookmarkList;
import svenhjol.strange.feature.bookmarks.Bookmarks;
import svenhjol.strange.feature.bookmarks.BookmarksClient;
import svenhjol.strange.feature.bookmarks.client.BookmarksButtons.NewBookmarkButton;
import svenhjol.strange.feature.bookmarks.client.BookmarksButtons.NewWhenEmptyButton;
import svenhjol.strange.feature.bookmarks.BookmarksResources;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.CloseButton;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.EditButton;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.NextPageButton;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.PreviousPageButton;
import svenhjol.strange.feature.travel_journal.client.screen.TravelJournalScreen;

public class BookmarksScreen extends TravelJournalScreen {
    int page;
    boolean renderedPaginationButtons = false;
    boolean renderedEditButtons = false;

    public BookmarksScreen() {
        this(1);
    }

    public BookmarksScreen(int page) {
        super(BookmarksResources.BOOKMARKS_TITLE);
        this.page = page;

        PageTracker.set(() -> new BookmarksScreen(page));
    }

    @Override
    protected void init() {
        super.init();

        // Add footer buttons
        addRenderableWidget(new CloseButton(midX + 5, 220, b -> onClose()));
        addRenderableWidget(new NewBookmarkButton(midX - (NewBookmarkButton.WIDTH + 5), 220,
            b -> BookmarksClient.makeNewBookmark()));

        initShortcuts();

        renderedPaginationButtons = false;
        renderedEditButtons = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderBookmarks(guiGraphics, mouseX, mouseY);
    }

    protected void renderBookmarks(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var rows = 7;

        var bookmarks = getBookmarks().all();
        var pages = bookmarks.size() / rows;
        var index = (page - 1) * rows;

        if (bookmarks.isEmpty() && !renderedEditButtons) {
            addRenderableWidget(new NewWhenEmptyButton(midX - (NewWhenEmptyButton.WIDTH / 2), 45, b -> BookmarksClient.makeNewBookmark()));
        }

        for (var y = 0; y < rows; y++) {
            if (index >= bookmarks.size()) {
                continue;
            }

            var bookmark = bookmarks.get(index);
            var name = bookmark.name;

            if (!renderedEditButtons) {
                addRenderableWidget(new EditButton(midX - 87, 39 + (y * 21), 190, b -> openBookmark(bookmark), TextHelper.literal(name)));
            }

            guiGraphics.renderItem(bookmark.getItemStack(), midX - 108, 41 + (y * 21));
            index++;
        }

        renderedEditButtons = true;

        if (!renderedPaginationButtons) {
            if (page > 1) {
                addRenderableWidget(new PreviousPageButton(midX - 30, 185, b -> BookmarksClient.openBookmarksScreen(page - 1)));
            }
            if (page < pages || index < bookmarks.size()) {
                addRenderableWidget(new NextPageButton(midX + 10, 185, b -> BookmarksClient.openBookmarksScreen(page + 1)));
            }
            renderedPaginationButtons = true;
        }
    }

    protected BookmarkList getBookmarks() {
        var minecraft = Minecraft.getInstance();

        if (minecraft.player != null) {
            return Bookmarks.getBookmarks(minecraft.player);
        }

        return new BookmarkList();
    }

    protected void openBookmark(Bookmark bookmark) {
        Minecraft.getInstance().setScreen(new BookmarkScreen(bookmark));
    }
}

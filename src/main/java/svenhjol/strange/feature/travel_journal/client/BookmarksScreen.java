package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.travel_journal.Bookmark;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.TravelJournal;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;

import java.util.List;

public class BookmarksScreen extends BaseScreen {
    int page;
    boolean renderedPaginationButtons = false;
    boolean renderedEditButtons = false;

    public BookmarksScreen() {
        this(1);
    }

    public BookmarksScreen(int page) {
        super(TravelJournalResources.BOOKMARKS_TITLE);
        this.page = page;
        PageTracker.Screen.BOOKMARKS.set();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new CloseButton(midX - (CloseButton.WIDTH / 2), 220, b -> onClose()));
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
        var rows = 6;
        var perPage = rows;

        var bookmarks = getBookmarks();
        var pages = bookmarks.size() / perPage;
        var index = (page - 1) * perPage;

        for (var y = 0; y < rows; y++) {
            if (index >= bookmarks.size()) {
                continue;
            }

            var bookmark = bookmarks.get(index);
            var name = bookmark.name;

            if (!renderedEditButtons) {
                addRenderableWidget(new EditButton(midX - 87, 55 + (y * 24), 190, b -> openBookmark(bookmark), TextHelper.literal(name)));
            }

            guiGraphics.renderItem(bookmark.getItemStack(), midX - 108, 57 + (y * 24));
            index++;
        }

        renderedEditButtons = true;

        if (!renderedPaginationButtons) {
            if (page > 1) {
                addRenderableWidget(new PreviousPageButton(midX - 30, 75, b -> openBookmarks(page - 1)));
            }
            if (page < pages || index < bookmarks.size()) {
                addRenderableWidget(new NextPageButton(midX + 10, 75, b -> openBookmarks(page + 1)));
            }
            renderedPaginationButtons = true;
        }
    }

    protected List<Bookmark> getBookmarks() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return List.of();
        }

        var bookmarksHolder = TravelJournal.getBookmarks(minecraft.player).orElse(null);
        if (bookmarksHolder == null) {
            return List.of();
        }

        return bookmarksHolder.getBookmarks();
    }

    protected void openBookmark(Bookmark bookmark) {
        Minecraft.getInstance().setScreen(new BookmarkScreen(bookmark));
    }
}

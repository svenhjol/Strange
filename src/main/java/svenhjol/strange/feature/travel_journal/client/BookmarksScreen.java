package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.travel_journal.*;

import java.util.List;

public class BookmarksScreen extends BaseTravelJournalScreen {
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
        var rows = 7;
        var perPage = rows;

        var bookmarks = getBookmarks();
        var pages = bookmarks.size() / perPage;
        var index = (page - 1) * perPage;

        if (bookmarks.isEmpty() && !renderedEditButtons) {
            addRenderableWidget(new NewBookmarkButton(midX - (NewBookmarkButton.WIDTH / 2), 45, b -> TravelJournalClient.makeNewBookmark()));
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
                addRenderableWidget(new PreviousPageButton(midX - 30, 185, b -> openBookmarks(page - 1)));
            }
            if (page < pages || index < bookmarks.size()) {
                addRenderableWidget(new NextPageButton(midX + 10, 185, b -> openBookmarks(page + 1)));
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

    static class NewBookmarkButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.NEW_BOOKMARK_BUTTON_TEXT;

        protected NewBookmarkButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }
}

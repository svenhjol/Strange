package svenhjol.strange.module.journals2.screen.bookmark;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.Bookmarks;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;
import svenhjol.strange.module.journals2.paginator.BookmarkPaginator;
import svenhjol.strange.module.journals2.screen.JournalPaginatedScreen;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class JournalBookmarksScreen extends JournalPaginatedScreen<Bookmark> {
    public JournalBookmarksScreen() {
        super(BOOKMARKS);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        paginator.render(poseStack, itemRenderer, font);
    }

    protected List<Bookmark> getPlayerBookmarks() {
        if (BookmarksClient.branch == null) return List.of();
        return BookmarksClient.branch.values(minecraft.player.getUUID());
    }

    @Override
    public BookmarkPaginator getPaginator() {
        var bookmarks = getPlayerBookmarks();
        return new BookmarkPaginator(bookmarks);
    }

    @Override
    protected Consumer<Bookmark> onClick() {
        return bookmark -> minecraft.setScreen(new JournalBookmarkScreen(bookmark));
    }

    @Override
    protected void addButtons() {
        // If there are no bookmarks, make an "Add bookmark" button.
        if (getPlayerBookmarks().size() == 0) {
            int buttonWidth = 140;
            int buttonHeight = 20;
            var button = new Button(midX - (buttonWidth / 2), 40, buttonWidth, buttonHeight, ADD_BOOKMARK, b -> addBookmark());
            addRenderableWidget(button);
        }

        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> addBookmark(), ADD_BOOKMARK));
    }

    @Override
    protected void setViewedPage() {
        Journals2Client.tracker.setPage(PageTracker.Page.BOOKMARKS);
    }

    protected void addBookmark() {
        if (getPlayerBookmarks().size() >= Bookmarks.maxBookmarksPerPlayer) {
            // TODO: some kind of message saying you've reached your limit
            return;
        }

        BookmarksClient.sendAddBookmark();
    }
}

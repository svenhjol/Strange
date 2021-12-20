package svenhjol.strange.module.journals2.screen.bookmark;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import svenhjol.strange.helper.GuiHelper.ButtonDefinition;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.Bookmarks;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;
import svenhjol.strange.module.journals2.paginator.BookmarkPaginator;
import svenhjol.strange.module.journals2.screen.JournalScreen;

import java.util.List;

public class JournalBookmarksScreen extends JournalScreen {
    private BookmarkPaginator paginator;

    public JournalBookmarksScreen() {
        super(BOOKMARKS);
        Journals2Client.tracker.setPage(PageTracker.Page.BOOKMARKS);
    }

    @Override
    protected void init() {
        super.init();

        // Fetch the player's bookmarks.
        var bookmarks = getPlayerBookmarks();

        // If there are no bookmarks, make an "Add bookmark" button.
        if (bookmarks.size() == 0) {
            int buttonWidth = 140;
            int buttonHeight = 20;
            var button = new Button(midX - (buttonWidth / 2), 40, buttonWidth, buttonHeight, ADD_BOOKMARK, b -> add());
            addRenderableWidget(button);
        }

        // Set up the bookmarks paginator.
        paginator = new BookmarkPaginator(bookmarks);

        paginator.init(this, offset, midX, 40, newOffset -> {
            offset = newOffset;
            init(minecraft, width, height);
        });

        bottomButtons.add(0, new ButtonDefinition(b -> add(), ADD_BOOKMARK));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        paginator.render(poseStack, itemRenderer, font);
    }

    protected void add() {
        if (getPlayerBookmarks().size() >= Bookmarks.maxBookmarksPerPlayer) {
            // TODO: some kind of message saying you've reached your limit
            return;
        }

        BookmarksClient.sendAddBookmark();
    }

    protected List<Bookmark> getPlayerBookmarks() {
        if (BookmarksClient.branch == null) return List.of();
        return BookmarksClient.branch.values(minecraft.player.getUUID());
    }
}

package svenhjol.strange.module.journals.screen.bookmark;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.journals.helper.JournalClientHelper;
import svenhjol.strange.module.journals.paginator.BookmarkPaginator;
import svenhjol.strange.module.journals.screen.JournalPaginatedScreen;
import svenhjol.strange.module.journals.screen.JournalResources;

import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class JournalBookmarksScreen extends JournalPaginatedScreen<Bookmark> {
    public JournalBookmarksScreen() {
        super(JournalResources.BOOKMARKS);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        paginator.render(poseStack, itemRenderer, font);
    }

    @Override
    public BookmarkPaginator getPaginator() {
        var bookmarks = JournalClientHelper.getPlayerBookmarks();
        return new BookmarkPaginator(bookmarks);
    }

    @Override
    protected Consumer<Bookmark> onClick() {
        return bookmark -> minecraft.setScreen(new JournalBookmarkScreen(bookmark));
    }

    @Override
    protected void addButtons() {
        // If there are no bookmarks, make an "Add bookmark" button.
        if (JournalClientHelper.getPlayerBookmarks().size() == 0) {
            int buttonWidth = 140;
            int buttonHeight = 20;
            var button = new Button(midX - (buttonWidth / 2), 40, buttonWidth, buttonHeight, JournalResources.ADD_BOOKMARK, b -> JournalClientHelper.addBookmark());
            addRenderableWidget(button);
        }

        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> JournalClientHelper.addBookmark(), JournalResources.ADD_BOOKMARK));
    }

    @Override
    protected void setViewedPage() {
        JournalsClient.tracker.setPage(PageTracker.Page.BOOKMARKS);
    }

}

package svenhjol.strange.module.journals.screen.bookmark;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalViewer;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.journals.screen.JournalScreen;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JournalBookmarksScreen extends JournalScreen {
    public JournalBookmarksScreen() {
        super(BOOKMARKS);

        // "add bookmark" button to the bottom
        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> add(), ADD_BOOKMARK));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        JournalViewer.viewedPage(Journals.Page.BOOKMARKS, lastPage);

        int buttonWidth = 140;
        int buttonHeight = 20;
        int yOffset = 21;

        if (journal == null || journal.getBookmarks() == null || journal.getBookmarks().size() == 0) {
            // no bookmarks, show "add bookmark" button and exit early
            if (!hasRenderedButtons) {
                addRenderableWidget(new Button(midX - (buttonWidth / 2), 40, buttonWidth, buttonHeight, ADD_BOOKMARK, b -> add()));
                hasRenderedButtons = true;
            }
            return;
        }

        AtomicInteger y = new AtomicInteger(40);
        Supplier<Component> labelForNoItem = () -> NO_BOOKMARKS;
        Consumer<JournalBookmark> renderItem = bookmark -> {
            String name = getTruncatedName(bookmark.getName(), 27);
            ItemStack icon = bookmark.getIcon();

            // render item icons each time
            itemRenderer.renderGuiItem(icon, midX - (buttonWidth / 2) - 12, y.get() + 2);

            // only render buttons on the first render pass
            if (!hasRenderedButtons) {
                Button button = new Button(midX - (buttonWidth / 2) + 6, y.get(), buttonWidth, buttonHeight, new TextComponent(name), b -> select(bookmark));
                addRenderableWidget(button);
            }

            y.addAndGet(yOffset);
        };

        paginator(poseStack, journal.getBookmarks(), renderItem, labelForNoItem, !hasRenderedButtons);
        hasRenderedButtons = true;
    }

    protected void select(JournalBookmark bookmark) {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalBookmarkScreen(bookmark)));
    }

    protected void add() {
        JournalsClient.sendAddBookmark();
    }
}

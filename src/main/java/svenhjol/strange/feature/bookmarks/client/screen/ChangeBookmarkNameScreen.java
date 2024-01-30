package svenhjol.strange.feature.bookmarks.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.bookmarks.Bookmark;
import svenhjol.strange.feature.bookmarks.BookmarksClient;
import svenhjol.strange.feature.bookmarks.BookmarksResources;
import svenhjol.strange.feature.travel_journal.client.screen.TravelJournalScreen;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.*;

public class ChangeBookmarkNameScreen extends TravelJournalScreen {
    protected Bookmark originalBookmark;
    protected Bookmark updatedBookmark;
    protected EditBox nameEditBox;
    protected int doubleClickTicks = 0;

    public ChangeBookmarkNameScreen(Bookmark bookmark) {
        super(BookmarksResources.CHANGE_NAME_TITLE);
        this.originalBookmark = bookmark;
        this.updatedBookmark = bookmark.copy();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new SaveButton(midX + 5,220, b -> save()));
        addRenderableWidget(new CancelButton(midX - (CancelButton.WIDTH + 5), 220, b -> back()));

        nameEditBox = new EditBox(font, midX - 74, 38, 148, 12,
            TextHelper.translatable("gui.strange.travel_journal.edit_name"));

        nameEditBox.setFocused(true);
        nameEditBox.setCanLoseFocus(false);
        nameEditBox.setTextColor(-1);
        nameEditBox.setTextColorUneditable(-1);
        nameEditBox.setBordered(true);
        nameEditBox.setMaxLength(32);
        nameEditBox.setResponder(val -> updatedBookmark.name = val);
        nameEditBox.setValue(updatedBookmark.name);
        nameEditBox.setEditable(true);

        addRenderableWidget(nameEditBox);
        setFocused(nameEditBox);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var xa = midX - 74;
        var ya = 38;
        var xb = midX + 74;
        var yb = 38 + 12;

        if (mouseX > xa && mouseX < xb
            && mouseY > ya && mouseY < yb) {
            if (doubleClickTicks == 0) {
                doubleClickTicks = 1;
            } else {
                nameEditBox.setCursorPosition(updatedBookmark.name.length());
                nameEditBox.setHighlightPos(0);
                doubleClickTicks = 0;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 257) { // return
            save();
            return true;
        }
        if (i == 256) { // escape
            back();
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        nameEditBox.render(guiGraphics, mouseX, mouseY, delta);

        if (doubleClickTicks > 0 && ++doubleClickTicks >= 40) {
            doubleClickTicks = 0;
        }
    }

    protected void save() {
        if (!updatedBookmark.name.isEmpty()) {
            BookmarksClient.changeBookmark(updatedBookmark);
        }
    }

    protected void back() {
        Minecraft.getInstance().setScreen(new BookmarkScreen(originalBookmark));
    }
}

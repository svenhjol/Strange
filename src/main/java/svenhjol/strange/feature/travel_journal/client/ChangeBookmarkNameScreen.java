package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.travel_journal.Bookmark;
import svenhjol.strange.feature.travel_journal.TravelJournalClient;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;

public class ChangeBookmarkNameScreen extends BaseScreen {
    protected Bookmark originalBookmark;
    protected Bookmark updatedBookmark;
    protected EditBox nameEditBox;

    public ChangeBookmarkNameScreen(Bookmark bookmark) {
        super(TravelJournalResources.CHANGE_NAME_TITLE);
        this.originalBookmark = bookmark;
        this.updatedBookmark = bookmark.copy();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new SaveButton(midX + 5,220, b -> save()));
        addRenderableWidget(new CancelButton(midX - (CancelButton.WIDTH + 5), 220, b -> back()));

        nameEditBox = new EditBox(font, (width / 2) - 72, 38, 149, 12,
            TextHelper.translatable("gui.strange.travel_journal.edit_name"));

        nameEditBox.setFocused(true);
        nameEditBox.setCanLoseFocus(false);
        nameEditBox.setTextColor(-1);
        nameEditBox.setTextColorUneditable(-1);
        nameEditBox.setBordered(true);
        nameEditBox.setMaxLength(32);
        nameEditBox.setResponder(val -> this.updatedBookmark.name = val);
        nameEditBox.setValue(this.updatedBookmark.name);
        nameEditBox.setEditable(true);

        addRenderableWidget(nameEditBox);
        setFocused(nameEditBox);
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
    }

    protected void save() {
        TravelJournalClient.changeBookmark(updatedBookmark);
    }

    protected void back() {
        Minecraft.getInstance().setScreen(new BookmarkScreen(originalBookmark));
    }
}

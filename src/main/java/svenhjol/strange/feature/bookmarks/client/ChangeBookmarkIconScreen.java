package svenhjol.strange.feature.bookmarks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.feature.bookmarks.Bookmark;
import svenhjol.strange.feature.bookmarks.BookmarksClient;
import svenhjol.strange.feature.bookmarks.BookmarksNetwork;
import svenhjol.strange.feature.travel_journal.client.BaseTravelJournalScreen;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons;
import svenhjol.strange.feature.travel_journal.client.TravelJournalResources;

public class ChangeBookmarkIconScreen extends BaseTravelJournalScreen {
    protected Bookmark originalBookmark;
    protected Bookmark updatedBookmark;
    protected ChangeBookmarkIconScreen(Bookmark bookmark) {
        super(TravelJournalResources.CHANGE_ICON_TITLE);
        this.originalBookmark = bookmark;
        this.updatedBookmark = bookmark.copy();
    }

    @Override
    protected void init() {
        super.init();

        // Ask the server for an updated list of item icons.
        BookmarksNetwork.RequestItemIcons.send();

        addRenderableWidget(new TravelJournalButtons.CancelButton(midX - (TravelJournalButtons.CancelButton.WIDTH / 2), 220, this::back));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        var icons = BookmarksClient.BOOKMARK_ICONS;
        var currentIcon = BuiltInRegistries.ITEM.get(updatedBookmark.item);
        int index = 0;

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 14; x++) {
                if (index >= icons.size()) {
                    continue;
                }

                var icon = icons.get(index);
                var xx = midX - 113 + (x * 16);
                var yy = 39 + (y * 17);

                if (icon == currentIcon) {
                    guiGraphics.fill(xx, yy, xx + 16, yy + 16, 0xee66ee55);
                }

                if (mouseX > xx && mouseX < xx + 16
                    && mouseY > yy && mouseY < yy + 16) {
                    guiGraphics.fill(xx, yy, xx + 16, yy + 16, 0x6666ee55);
                }

                guiGraphics.renderFakeItem(new ItemStack(icon), xx, yy);

                index++;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var icons = BookmarksClient.BOOKMARK_ICONS;
        int index = 0;

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 14; x++) {
                if (index >= icons.size()) {
                    continue;
                }

                var xx = midX - 113 + (x * 16);
                var yy = 39 + (y * 17);

                if (mouseX > xx && mouseX < xx + 16
                    && mouseY > yy && mouseY < yy + 16) {
                    updatedBookmark.item = BuiltInRegistries.ITEM.getKey(icons.get(index));
                    save();
                    return true;
                }

                index++;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void save() {
        BookmarksClient.changeBookmark(updatedBookmark);
    }

    protected void back(Button button) {
        Minecraft.getInstance().setScreen(new BookmarkScreen(originalBookmark));
    }
}

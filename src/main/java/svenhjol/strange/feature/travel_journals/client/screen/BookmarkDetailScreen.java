package svenhjol.strange.feature.travel_journals.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.strange.feature.core.client.CoreButtons;
import svenhjol.strange.feature.travel_journals.client.Resources;
import svenhjol.strange.feature.travel_journals.common.BookmarkData;

public class BookmarkDetailScreen extends BaseScreen {
    private final ItemStack stack;
    private final BookmarkData.Mutable bookmark;
    private EditBox name;
    private MultiLineEditBox description;

    private boolean hasMap;
    private boolean hasPaper;
    
    public BookmarkDetailScreen(ItemStack stack, BookmarkData bookmark) {
        super(Component.literal(bookmark.name()));
        
        this.stack = stack;
        this.bookmark = new BookmarkData.Mutable(bookmark);
    }

    @Override
    protected void init() {
        super.init();
        
        var inputWidth = 210;
        var nameHeight = 13;
        var descriptionHeight = 46;
        var top = 46;
        
        name = new EditBox(font, midX - (inputWidth / 2), top, inputWidth, nameHeight, Resources.EDIT_NAME);
        name.setFocused(true);
        name.setValue(bookmark.name());
        name.setResponder(bookmark::name);
        name.setCanLoseFocus(true);
        name.setTextColor(-1);
        name.setTextColorUneditable(-1);
        name.setBordered(true);
        name.setMaxLength(32);
        name.setEditable(true);

        addRenderableWidget(name);
        setFocused(name);
        
        
        description = new MultiLineEditBox(font, midX - (inputWidth / 2), top + 28, inputWidth, descriptionHeight,
            Resources.EDIT_DESCRIPTION, Component.empty());

        description.setFocused(false);
        description.setValue(bookmark.description());
        description.setValueListener(bookmark::description);
        description.setCharacterLimit(140);
        
        addRenderableWidget(description);

        top = 220;
        addRenderableWidget(new CoreButtons.DeleteButton((int) (midX - (CoreButtons.DeleteButton.WIDTH * 1.5)) - 5, top,
            b -> deleteAndClose()));
        addRenderableWidget(new CoreButtons.CancelButton(midX - (CoreButtons.CancelButton.WIDTH / 2), top,
            b -> onClose()));
        addRenderableWidget(new CoreButtons.SaveButton(midX + (CoreButtons.SaveButton.WIDTH / 2) + 5, top,
            b -> saveAndClose()));

        hasMap = hasMap();
        hasPaper = hasPaper();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        
        name.render(guiGraphics, mouseX, mouseY, delta);
        description.render(guiGraphics, mouseX, mouseY, delta);
        
        guiGraphics.drawString(font, Resources.NAME_TEXT, midX - 104, 36, 0x444444, false);
        guiGraphics.drawString(font, Resources.DESCRIPTION, midX - 104, 64, 0x444444, false);
    }

    private boolean hasMap() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        return minecraft.player.getInventory().contains(new ItemStack(Items.MAP));
    }

    private boolean hasPaper() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        return minecraft.player.getInventory().contains(new ItemStack(Items.PAPER));
    }
    
    private void saveAndClose() {
        // Validation first.
        if (this.bookmark.name().isEmpty()) {
            this.bookmark.name(Resources.NEW_BOOKMARK.getString());
        }

        feature().handlers.updateBookmark(stack, bookmark.toImmutable());
        onClose();
    }
    
    private void deleteAndClose() {
        feature().handlers.deleteBookmark(stack, bookmark.toImmutable());
        onClose();
    }

    @Override
    public void onClose() {
        feature().handlers.openBookmarks(stack);
    }
}

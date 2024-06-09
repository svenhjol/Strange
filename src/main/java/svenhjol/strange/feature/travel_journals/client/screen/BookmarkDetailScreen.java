package svenhjol.strange.feature.travel_journals.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.feature.core.client.CoreButtons;
import svenhjol.strange.feature.travel_journals.client.Buttons;
import svenhjol.strange.feature.travel_journals.client.Resources;
import svenhjol.strange.feature.travel_journals.common.BookmarkData;

public class BookmarkDetailScreen extends BaseScreen {
    private final ItemStack stack;
    private final BookmarkData.Mutable bookmark;
    private EditBox name;
    private MultiLineEditBox description;
    private ImageButton exportPageButton;
    private ImageButton exportMapButton;
    
    public BookmarkDetailScreen(ItemStack stack, BookmarkData bookmark) {
        super(Component.literal(bookmark.name()));
        
        this.stack = stack;
        this.bookmark = new BookmarkData.Mutable(bookmark);
    }

    @Override
    protected void init() {
        super.init();

        var handlers = feature().handlers;
        var inputWidth = 220;
        var nameHeight = 15;
        var descriptionHeight = 46;
        var top = 110;
        
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
        
        description = new MultiLineEditBox(font, midX - (inputWidth / 2), top + 29, inputWidth, descriptionHeight,
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

        exportPageButton = new Buttons.ExportPageButton(midX + 120, 30,
            b -> handlers.exportPage(bookmark.toImmutable()));
        
        exportMapButton = new Buttons.ExportMapButton(midX + 120, 47,
            b -> handlers.exportMap(bookmark.toImmutable()));

        addRenderableWidget(exportPageButton);
        addRenderableWidget(exportMapButton);
        
        exportPageButton.visible = false;
        exportMapButton.visible = false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        renderPhoto(guiGraphics);
        renderDimensionAndPosition(guiGraphics);
        renderUtilityButtons();
        
        name.render(guiGraphics, mouseX, mouseY, delta);
        description.render(guiGraphics, mouseX, mouseY, delta);
        
        var textColor = 0x404040;
        guiGraphics.drawString(font, Resources.NAME_TEXT, midX - 109, 101, textColor, false);
        guiGraphics.drawString(font, Resources.DESCRIPTION, midX - 109, 129, textColor, false);
    }

    private void renderUtilityButtons() {
        var handlers = feature().handlers;
        
        var y = 13;
        var lineHeight = 17;
        
        if (handlers.hasPaper()) {
            y += lineHeight;
            exportPageButton.visible = true;
            exportPageButton.setY(y);
        } else {
            exportPageButton.visible = false;
        }
        
        if (handlers.hasMap()) {
            y += lineHeight;
            exportMapButton.visible = true;
            exportMapButton.setY(y);
        } else {
            exportMapButton.visible = false;
        }
    }

    private void renderPhoto(GuiGraphics guiGraphics) {
        var pose = guiGraphics.pose();
        var resource = feature().handlers.tryLoadPhoto(bookmark.id());
        
        if (resource != null) {
            pose.pushPose();
            var top = 24; // This is scaled by pose.scale()
            var left = -169; // This is scaled by pose.scale()
            pose.translate(midX - 40f, 33f, 1.0f);
            pose.scale(0.41f, 0.22f, 1.0f);
            RenderSystem.setShaderTexture(0, resource);
            guiGraphics.blit(resource, left, top, 0, 0, 256, 256);
            pose.popPose();
        }
    }
    
    private void renderDimensionAndPosition(GuiGraphics guiGraphics) {
        var pose = guiGraphics.pose();
        var color = 0xb8907a;
        
        pose.pushPose();
        var top = 30; // This is scaled by pose.scale()
        var left = 43; // This is scaled by pose.scale()
        pose.translate(midX - 25f, 20f, 1.0f);
        pose.scale(0.82f, 0.82f, 1.0f);

        var pos = bookmark.pos();
        var dimension = bookmark.dimension();
        var positionText = Component.translatable(Resources.COORDINATES_KEY, pos.getX(), pos.getY(), pos.getZ());
        var dimensionText = Component.translatable(feature().handlers.dimensionLocaleKey(dimension));
        
        guiGraphics.drawString(font, Component.translatable(Resources.DIMENSION).withStyle(ChatFormatting.BOLD), left, top, color, false);
        guiGraphics.drawString(font, dimensionText, left, top + 12, color, false);

        guiGraphics.drawString(font, Component.translatable(Resources.POSITION).withStyle(ChatFormatting.BOLD), left, top + 30, color, false);
        guiGraphics.drawString(font, positionText, left, top + 42, color, false);
        pose.popPose();
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

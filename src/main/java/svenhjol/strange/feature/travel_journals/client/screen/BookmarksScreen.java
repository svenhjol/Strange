package svenhjol.strange.feature.travel_journals.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;
import svenhjol.strange.feature.core.client.CoreButtons;
import svenhjol.strange.feature.travel_journals.client.Buttons;
import svenhjol.strange.feature.travel_journals.client.ClientHelpers;
import svenhjol.strange.feature.travel_journals.client.Resources;
import svenhjol.strange.feature.travel_journals.common.BookmarkData;
import svenhjol.strange.feature.travel_journals.common.JournalData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BookmarksScreen extends BaseScreen {
    private final int page;
    private final int columns;
    private final ItemStack stack;
    private boolean renderedButtons = false;
    
    public BookmarksScreen(ItemStack stack, int page) {
        super(stack.getOrDefault(DataComponents.CUSTOM_NAME, Resources.TRAVEL_JOURNAL));
        this.stack = stack;
        this.page = page;
        this.columns = 2;
    }

    @Override
    protected void init() {
        super.init();

        // Add footer buttons
        addRenderableWidget(new CoreButtons.CloseButton(midX + 5, 220, b -> onClose()));
        addRenderableWidget(new Buttons.NewBookmarkButton(midX - (Buttons.NewBookmarkButton.WIDTH + 5), 220,
            b -> feature().handlers.makeNewBookmark()));

        renderedButtons = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        
        var bookmarks = bookmarks();
        var index = (page - 1) * columns;

        for (var x = 0; x < columns; x++) {
            if (index >= bookmarks.size()) continue;;
            
            var bookmark = bookmarks.get(index);
            
            renderTitleDetails(guiGraphics, bookmark, x);
            renderPhoto(guiGraphics, bookmark, x);
            renderPosition(guiGraphics, bookmark, x);
            renderDetailsButton(bookmark, x);
            
            index++;
        }

        if (bookmarks.isEmpty()) {
            renderWhenNoBookmarks();
        } else {
            renderPaginationButtons(index);
        }
        
        renderedButtons = true;
    }
    
    private void renderTitleDetails(GuiGraphics guiGraphics, BookmarkData bookmark, int x) {
        var pose = guiGraphics.pose();
        var name = bookmark.name();
        var extra = bookmark.extra();
        var author = extra.author();
        var color = extra.color();
        var timestamp = extra.timestamp();
        var date = new Date(timestamp * 1000L);
        var format = new SimpleDateFormat("dd-MMM-yy");
        var titleColor = 0x444444;
        var authorColor = color.getMapColor().calculateRGBColor(MapColor.Brightness.LOW);
        var maxTitleLength = 25;
        var left = -100 + (x * 169);
        var top = 33;
        
        // Render top text.
        pose.pushPose();
        pose.translate(midX - 40f, 20f, 1.0f);
        pose.scale(0.7f, 0.7f, 1.0f);

        if (name.length() > maxTitleLength) {
            name = name.substring(0, maxTitleLength - 1);
        }

        guiGraphics.drawString(font, Component.literal(name).withStyle(ChatFormatting.BOLD), left, top, titleColor, false);

        if (!author.isEmpty()) {
            top += 12;
            guiGraphics.drawString(font, Component.translatable(Resources.CREATED_BY_KEY, author), left, top, authorColor, false);
        }

        if (timestamp >= 0) {
            top += 12;
            guiGraphics.drawString(font, Component.literal(format.format(date)), left, top, authorColor, false);
        }
        pose.popPose();
    }
    
    private void renderPhoto(GuiGraphics guiGraphics, BookmarkData bookmark, int x) {
        var pose = guiGraphics.pose();
        
        var resource = feature().handlers.tryLoadPhoto(bookmark.id());
        if (resource != null) {
            var top = 127;
            var left = -168 + (x * 272);
            pose.pushPose();
            pose.translate(midX - 40f, 40f, 1.0f);
            pose.scale(0.42f, 0.24f, 1.0f);
            RenderSystem.setShaderTexture(0, resource);
            guiGraphics.blit(resource, left, top, 0, 0, 256, 256);
            pose.popPose();
        }
    }
    
    private void renderPosition(GuiGraphics guiGraphics, BookmarkData bookmark, int x) {
        var pose = guiGraphics.pose();
        var pos = bookmark.pos();
        var dimension = bookmark.dimension();
        var top = 202;
        var left = -95 + (x * 163);
        var positionColor = 0xaf9f8f;
        
        pose.pushPose();
        pose.translate(midX - 25f, 20f, 1.0f);
        pose.scale(0.7f, 0.7f, 1.0f);
        
        var positionText = Component.translatable(Resources.COORDINATES_KEY, pos.getX(), pos.getY(), pos.getZ());
        var dimensionText = Component.translatable(feature().handlers.dimensionLocaleKey(dimension));

        ClientHelpers.drawCenteredString(guiGraphics, font, dimensionText, left + 50, top, positionColor, false);
        ClientHelpers.drawCenteredString(guiGraphics, font, positionText, left + 50, top + 13, positionColor, false);
        pose.popPose();
    }
    
    private void renderDetailsButton(BookmarkData bookmark, int x) {
        var left = midX - 110 + (x * 114);
        var top = 135;

        if (!renderedButtons) {
            addRenderableWidget(new CoreButtons.EditButton(left, top, 107,
                b -> feature().handlers.openBookmark(stack, bookmark), Resources.DETAILS));
        }
    }
    
    private void renderWhenNoBookmarks() {
        if (!renderedButtons) {
            addRenderableWidget(new Buttons.NewWhenEmptyButton(midX - (Buttons.NewWhenEmptyButton.WIDTH / 2), 45,
                b -> feature().handlers.makeNewBookmark()));
        }
    }
    
    private void renderPaginationButtons(int index) {
        var size = bookmarks().size();
        var pages = size / columns;
        
        if (!renderedButtons) {
            if (page > 1) {
                addRenderableWidget(new CoreButtons.PreviousPageButton(midX - 110, 180,
                    b -> feature().handlers.openBookmarks(stack, page - 1)));
            }
            if (page < pages || index < size) {
                addRenderableWidget(new CoreButtons.NextPageButton(midX + 80, 180,
                    b -> feature().handlers.openBookmarks(stack, page + 1)));
            }
        }
    }
    
    private JournalData journal() {
        return JournalData.get(stack);
    }

    private List<BookmarkData> bookmarks() {
        return journal().bookmarks();
    }
}

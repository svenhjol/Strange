package svenhjol.strange.feature.travel_journals.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
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
import java.util.List;

public class BookmarksScreen extends BaseScreen {
    private final int page;
    private final ItemStack stack;
    private boolean renderedPaginationButtons = false;
    private boolean renderedBookmarkButtons = false;
    
    public BookmarksScreen(ItemStack stack, int page) {
        super(Resources.TRAVEL_JOURNAL_TITLE);
        this.stack = stack;
        this.page = page;
    }

    @Override
    protected void init() {
        super.init();

        // Add footer buttons
        addRenderableWidget(new CoreButtons.CloseButton(midX + 5, 220, b -> onClose()));
        addRenderableWidget(new Buttons.NewBookmarkButton(midX - (Buttons.NewBookmarkButton.WIDTH + 5), 220,
            b -> feature().handlers.makeNewBookmark()));

        renderedPaginationButtons = false;
        renderedBookmarkButtons = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        
        var columns = 2;
        var bookmarks = getBookmarks();
        var pages = bookmarks.size() / columns;
        var index = (page - 1) * columns;

        if (bookmarks.isEmpty() && !renderedBookmarkButtons) {
            addRenderableWidget(new Buttons.NewWhenEmptyButton(midX - (Buttons.NewWhenEmptyButton.WIDTH / 2), 45, 
                b -> feature().handlers.makeNewBookmark()));
        }

        for (var x = 0; x < columns; x++) {
            if (index >= bookmarks.size()) {
                continue;
            }

            var pose = guiGraphics.pose();
            var bookmark = bookmarks.get(index);
            var name = bookmark.name();
            var pos = bookmark.pos();
            var extra = bookmark.extra();
            var author = extra.author();
            var color = extra.color();
            var timestamp = extra.timestamp();
            
            var date = new java.util.Date(timestamp * 1000L);
            var format = new SimpleDateFormat("dd-MMM-yy");

            var titleColor = 0x444444;
            var authorColor = color.getMapColor().calculateRGBColor(MapColor.Brightness.LOW);
            var positionColor = 0xaf9f8f;
            var left = midX - 110 + (x * 114); // For normal text and buttons
            var scaledTextLeft = -100 + (x * 169); // For scaled text at the top
            var scaledPhotoLeft = -168 + (x * 272); // For scaled photo
            var top = 30;
            var maxTitleLength = 25;
            
            // Render top text.
            pose.pushPose();
            pose.translate(midX - 40f, 20f, 1.0f);
            pose.scale(0.7f, 0.7f, 1.0f);
            
            if (name.length() > maxTitleLength) {
                name = name.substring(0, maxTitleLength - 1);
            }
            
            guiGraphics.drawString(font, Component.literal(name).withStyle(ChatFormatting.BOLD), scaledTextLeft, top, titleColor, false);
            
            if (!author.isEmpty()) {
                top += 12;
                guiGraphics.drawString(font, Component.translatable(Resources.CREATED_BY_KEY, author), scaledTextLeft, top, authorColor, false);
            }
            
            if (timestamp >= 0) {
                top += 12;
                guiGraphics.drawString(font, Component.literal(format.format(date)), scaledTextLeft, top, authorColor, false);
            }
            pose.popPose();

            // Render photo.
            pose.pushPose();
            
            var resource = feature().handlers.tryLoadPhoto(bookmark.id());
            if (resource != null) {
                top = 118; // Translated top!
                pose.translate(midX - 40f, 20f, 1.0f);
                pose.scale(0.42f, 0.42f, 1.0f);
                RenderSystem.setShaderTexture(0, resource);
                guiGraphics.blit(resource, scaledPhotoLeft, top, 0, 30, 256, 160);
            }
            pose.popPose();
            
            // Render dimension, position and edit button.
            top = 141;
            if (!renderedBookmarkButtons) {
                addRenderableWidget(new CoreButtons.EditButton(left, top, 107,
                    b -> feature().handlers.openBookmark(bookmark), Resources.EDIT_BOOKMARK_BUTTON));
            }

            pose.pushPose();
            top = 208; // Translated top!
            
            pose.translate(midX - 25f, 20f, 1.0f);
            pose.scale(0.7f, 0.7f, 1.0f);
            var positionText = Component.translatable(Resources.COORDINATES_KEY, pos.getX(), pos.getY(), pos.getZ());
            ClientHelpers.drawCenteredString(guiGraphics, font, positionText, scaledTextLeft + 50, top, positionColor, false);
            pose.popPose();
            
            index++;
        }
        renderedBookmarkButtons = true;

        if (!renderedPaginationButtons) {
            if (page > 1) {
                addRenderableWidget(new CoreButtons.PreviousPageButton(midX - 110, 180, 
                    b -> feature().handlers.openBookmarks(stack, page - 1)));
            }
            if (page < pages || index < bookmarks.size()) {
                addRenderableWidget(new CoreButtons.NextPageButton(midX + 80, 180,
                    b -> feature().handlers.openBookmarks(stack, page + 1)));
            }
            renderedPaginationButtons = true;
        }
    }
    
    protected JournalData getJournal() {
        return JournalData.get(stack);
    }

    protected List<BookmarkData> getBookmarks() {
        return getJournal().bookmarks();
    }
}

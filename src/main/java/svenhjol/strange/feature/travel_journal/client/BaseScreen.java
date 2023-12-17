package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;

public abstract class BaseScreen extends Screen {
    protected int midX;
    protected int midY;
    protected int backgroundWidth;
    protected int backgroundHeight;

    protected BaseScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null) {
            return;
        }

        midX = width / 2;
        midY = height / 2;

        backgroundWidth = TravelJournalResources.JOURNAL_BACKGROUND_DIM.getFirst();
        backgroundHeight = TravelJournalResources.JOURNAL_BACKGROUND_DIM.getSecond();
    }

    protected void initShortcuts() {
        addRenderableWidget(new HomeShortcutButton(midX + 120, midY - 80, this::openHome));
        addRenderableWidget(new BookmarksShortcutButton(midX + 120, midY - 62, this::openBookmarks));
        addRenderableWidget(new LearnedShortcutButton(midX + 120, midY - 44, this::openLearned));
    }

    protected void openHome(Button button) {
        if (!(this instanceof HomeScreen)) {
            Minecraft.getInstance().setScreen(new HomeScreen());
        }
    }

    protected void openBookmarks(Button button) {
        if (!(this instanceof BookmarksScreen)) {
            Minecraft.getInstance().setScreen(new BookmarksScreen());
        }
    }

    protected void openLearned(Button button) {
        if (!(this instanceof LearnedScreen)) {
            Minecraft.getInstance().setScreen(new LearnedScreen());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTitle(guiGraphics, midX, midY - 80);
    }

    protected void renderTitle(GuiGraphics guiGraphics, int x, int y) {
        drawCenteredString(guiGraphics, getTitle(), x, y, 0x555555, false);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        guiGraphics.blit(getBackgroundTexture(), x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    protected ResourceLocation getBackgroundTexture() {
        return TravelJournalResources.JOURNAL_BACKGROUND;
    }

    /**
     * Version of drawCenteredString that allows specifying of drop shadow.
     * @see GuiGraphics#drawCenteredString(Font, Component, int, int, int) 
     */
    protected void drawCenteredString(GuiGraphics guiGraphics, Component component, int x, int y, int color, boolean dropShadow) {
        var formattedCharSequence = component.getVisualOrderText();
        guiGraphics.drawString(font, formattedCharSequence, x - font.width(formattedCharSequence) / 2, y, color, dropShadow);
    }

    static class CloseButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CLOSE_BUTTON_TEXT;
        public CloseButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class HomeShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.HOME_BUTTON;
        static Component TEXT = TravelJournalResources.HOME_BUTTON_TEXT;

        protected HomeShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress, TEXT);
        }
    }

    static class BookmarksShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.BOOKMARKS_BUTTON;
        static Component TEXT = TravelJournalResources.BOOKMARKS_BUTTON_TEXT;

        protected BookmarksShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress, TEXT);
        }
    }

    static class LearnedShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.LEARNED_BUTTON;
        static Component TEXT = TravelJournalResources.LEARNED_BUTTON_TEXT;

        protected LearnedShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress, TEXT);
        }
    }
}
package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.runestones.Runestones;
import svenhjol.strange.feature.travel_journal.TravelJournal;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;

public abstract class BaseTravelJournalScreen extends Screen {
    protected int midX;
    protected int backgroundWidth;
    protected int backgroundHeight;

    protected BaseTravelJournalScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null) {
            return;
        }

        midX = width / 2;

        backgroundWidth = TravelJournalResources.JOURNAL_BACKGROUND_DIM.getFirst();
        backgroundHeight = TravelJournalResources.JOURNAL_BACKGROUND_DIM.getSecond();
    }

    protected void initShortcuts() {
        var yOffset = 30;
        var lineHeight = 17;
        var loader = Mods.common(Strange.ID).loader();

        addRenderableWidget(new HomeShortcutButton(midX + 120, yOffset, this::openHome));
        yOffset += lineHeight;

        addRenderableWidget(new BookmarksShortcutButton(midX + 120, yOffset, this::openBookmarks));
        yOffset += lineHeight;

        if (loader.isEnabled(Runestones.class)) {
            addRenderableWidget(new LearnedShortcutButton(midX + 120, yOffset, this::openLearned));
            yOffset += lineHeight;
        }

        if (loader.isEnabled(Quests.class)) {
            addRenderableWidget(new QuestsShortcutButton(midX + 120, yOffset, this::openQuests));
            yOffset += lineHeight;
        }
    }

    protected void openHome(Button button) {
        Minecraft.getInstance().setScreen(new HomeScreen());
    }

    protected void openBookmarks(Button button) {
        Minecraft.getInstance().setScreen(new BookmarksScreen());
    }

    protected void openBookmarks(int page) {
        Minecraft.getInstance().setScreen(new BookmarksScreen(page));
    }

    protected void openQuests(Button button) {
        Minecraft.getInstance().setScreen(new QuestsScreen());
    }

    protected void openQuests(int page) {
        Minecraft.getInstance().setScreen(new QuestsScreen(page));
    }

    protected void openLearned(Button button) {
        this.openLearned(1);
    }

    protected void openLearned(int page) {
        Minecraft.getInstance().setScreen(new LearnedScreen(page));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTitle(guiGraphics, midX, 24);
    }

    protected void renderTitle(GuiGraphics guiGraphics, int x, int y) {
        drawCenteredString(guiGraphics, getTitle(), x, y, 0xa05f50, false);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = 5;
        guiGraphics.blit(getBackgroundTexture(), x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void onClose() {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(TravelJournal.interactSound.get(), 0.5f, 1.0f);
        }
        super.onClose();
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

    protected static ILog log() {
        return Mods.client(Strange.ID).log();
    }

    static class CloseButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CLOSE_BUTTON_TEXT;
        public CloseButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class BackButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.BACK_BUTTON_TEXT;
        public BackButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class SaveButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.SAVE_BUTTON_TEXT;
        public SaveButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class CancelButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CANCEL_BUTTON_TEXT;
        public CancelButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class EditButton extends Button {
        static int HEIGHT = 20;

        protected EditButton(int x, int y, int width, OnPress onPress, Component text) {
            super(x, y, width, HEIGHT, text, onPress, DEFAULT_NARRATION);
        }
    }

    static class HomeShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.HOME_BUTTON;
        static Component TEXT = TravelJournalResources.HOME_BUTTON_TEXT;

        protected HomeShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static class BookmarksShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.BOOKMARKS_BUTTON;
        static Component TEXT = TravelJournalResources.BOOKMARKS_BUTTON_TEXT;

        protected BookmarksShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static class LearnedShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.LEARNED_BUTTON;
        static Component TEXT = TravelJournalResources.LEARNED_BUTTON_TEXT;

        protected LearnedShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static class QuestsShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.QUESTS_BUTTON;
        static Component TEXT = TravelJournalResources.QUESTS_BUTTON_TEXT;

        protected QuestsShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static class NextPageButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 19;
        static WidgetSprites SPRITES = TravelJournalResources.NEXT_PAGE_BUTTON;
        static Component TEXT = TravelJournalResources.NEXT_PAGE_BUTTON_TEXT;

        protected NextPageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static class PreviousPageButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 19;
        static WidgetSprites SPRITES = TravelJournalResources.PREVIOUS_PAGE_BUTTON;
        static Component TEXT = TravelJournalResources.PREVIOUS_PAGE_BUTTON_TEXT;

        protected PreviousPageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }
}
package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
import svenhjol.strange.helper.GuiHelper;

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

        addRenderableWidget(new Buttons.HomeShortcutButton(midX + 120, yOffset, this::openHome));
        yOffset += lineHeight;

        addRenderableWidget(new Buttons.BookmarksShortcutButton(midX + 120, yOffset, this::openBookmarks));
        yOffset += lineHeight;

        if (loader.isEnabled(Runestones.class)) {
            addRenderableWidget(new Buttons.LearnedShortcutButton(midX + 120, yOffset, this::openLearned));
            yOffset += lineHeight;
        }

        if (loader.isEnabled(Quests.class)) {
            addRenderableWidget(new Buttons.QuestsShortcutButton(midX + 120, yOffset, this::openQuests));
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
        GuiHelper.drawCenteredString(guiGraphics, font, getTitle(), x, y, 0xa05f50, false);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);

        int x = (width - backgroundWidth) / 2;
        int y = 5;
        guiGraphics.blit(getBackgroundTexture(), x, y, 0, 0, backgroundWidth, backgroundHeight);
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

    protected static ILog log() {
        return Mods.client(Strange.ID).log();
    }
}
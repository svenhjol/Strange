package svenhjol.strange.feature.travel_journal.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.travel_journal.TravelJournal;
import svenhjol.strange.feature.travel_journal.TravelJournalClient;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons;
import svenhjol.strange.feature.travel_journal.client.TravelJournalResources;
import svenhjol.strange.helper.GuiHelper;

public abstract class TravelJournalScreen extends Screen {
    protected int midX;
    protected int backgroundWidth;
    protected int backgroundHeight;

    protected TravelJournalScreen(Component component) {
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
        var xo = midX + 120;
        var yo = 30;
        var lineHeight = 17;

        // Shortcut to home always visible.
        addRenderableWidget(new TravelJournalButtons.HomeShortcutButton(xo, yo,
            TravelJournalClient::openHomeScreen));
        yo += lineHeight;

        // Iterate over registered shortcuts.
        for (var shortcut : TravelJournalClient.SHORTCUTS) {
            addRenderableWidget(shortcut.apply(xo, yo));
            yo += lineHeight;
        }
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
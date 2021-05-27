package svenhjol.strange.module.travel_journals.screen;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class TravelJournalHomeScreen extends TravelJournalBaseScreen {
    private boolean hasRenderedNavButtons = false;

    public TravelJournalHomeScreen() {
        super(I18n.translate("item.strange.travel_journal"));
        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();

        if (previousPage != null) {
            switch (previousPage) {
                case ENTRIES:
                    openEntriesScreen();
                    return;
                case SCROLLS:
                    openScrollsScreen();
                    return;
                case RUNES:
                    openRunesScreen();
                    return;
            }
        }

        hasRenderedNavButtons = false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        int mid = this.width / 2;
        int top = 34;
        int left = mid - 88;

        // draw title
        centeredString(matrices, textRenderer, I18n.translate("gui.strange.travel_journal.welcome"), mid, titleTop, TEXT_COLOR);

        // draw info
        int yOffset = top + 6;
        for (int i = 1; i <= 3; i++) {
            textRenderer.draw(matrices, I18n.translate("gui.strange.travel_journal.home_text_" + i), left, yOffset, TEXT_COLOR);
            yOffset += textRowHeight;
        }

        if (!hasRenderedNavButtons) {
            yOffset = 62;
            int rowHeight = 24;
            int buttonWidth = 100;
            int buttonHeight = 20;

            yOffset += rowHeight;
            this.addDrawableChild(new ButtonWidget((width / 2) - (buttonWidth / 2), yOffset, buttonWidth, buttonHeight, new TranslatableText("gui.strange.travel_journal.open_entries"), button -> this.openEntriesScreen()));

            yOffset += rowHeight;
            this.addDrawableChild(new ButtonWidget((width / 2) - (buttonWidth / 2), yOffset, buttonWidth, buttonHeight, new TranslatableText("gui.strange.travel_journal.open_runes"), button -> this.openRunesScreen()));

            yOffset += rowHeight;
            this.addDrawableChild(new ButtonWidget((width / 2) - (buttonWidth / 2), yOffset, buttonWidth, buttonHeight, new TranslatableText("gui.strange.travel_journal.open_scrolls"), button -> this.openScrollsScreen()));
        }
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addDrawableChild(new ButtonWidget((width / 2) - (w / 2), y, w, h, new TranslatableText("gui.strange.travel_journal.close"), button -> onClose()));
    }
}

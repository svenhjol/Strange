package svenhjol.strange.module.travel_journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

public class TravelJournalHomeScreen extends TravelJournalBaseScreen {
    private boolean hasRenderedNavButtons = false;

    public TravelJournalHomeScreen() {
        super(I18n.get("item.strange.travel_journal"));
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
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        int mid = this.width / 2;
        int top = 34;
        int left = mid - 88;

        // draw title
        centeredString(matrices, font, I18n.get("gui.strange.travel_journal.welcome"), mid, titleTop, TEXT_COLOR);

        // draw info
        int yOffset = top + 6;
        for (int i = 1; i <= 3; i++) {
            font.draw(matrices, I18n.get("gui.strange.travel_journal.home_text_" + i), left, yOffset, TEXT_COLOR);
            yOffset += textRowHeight;
        }

        if (!hasRenderedNavButtons) {
            yOffset = 62;
            int rowHeight = 24;
            int buttonWidth = 100;
            int buttonHeight = 20;

            yOffset += rowHeight;
            this.addRenderableWidget(new Button((width / 2) - (buttonWidth / 2), yOffset, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.travel_journal.open_entries"), button -> this.openEntriesScreen()));

            yOffset += rowHeight;
            this.addRenderableWidget(new Button((width / 2) - (buttonWidth / 2), yOffset, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.travel_journal.open_runes"), button -> this.openRunesScreen()));

            yOffset += rowHeight;
            this.addRenderableWidget(new Button((width / 2) - (buttonWidth / 2), yOffset, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.travel_journal.open_scrolls"), button -> this.openScrollsScreen()));
        }
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addRenderableWidget(new Button((width / 2) - (w / 2), y, w, h, new TranslatableComponent("gui.strange.travel_journal.close"), button -> onClose()));
    }
}

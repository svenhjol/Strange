package svenhjol.strange.traveljournals.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;

public class TravelJournalScreen extends TravelJournalBaseScreen {
    public TravelJournalScreen() {
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
                    openScrollScreen();
                    return;
                case RUNES:
                    openRunesScreen();
                    return;
            }
        }

        // do home things here!
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addButton(new ButtonWidget((width / 2) - (w / 2), y, w, h, new TranslatableText("gui.strange.travel_journal.close"), button -> onClose()));
    }
}

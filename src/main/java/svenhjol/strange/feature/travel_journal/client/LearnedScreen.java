package svenhjol.strange.feature.travel_journal.client;

import svenhjol.strange.feature.travel_journal.TravelJournalResources;

public class LearnedScreen extends BaseScreen {
    protected LearnedScreen() {
        super(TravelJournalResources.LEARNED_TITLE);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new CloseButton(midX - (CloseButton.WIDTH / 2),midY + 105, b -> onClose()));

        initShortcuts();
    }
}

package svenhjol.strange.feature.travel_journal.client.screen;

import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.TravelJournalClient;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.*;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;

public class HomeScreen extends TravelJournalScreen {
    public HomeScreen() {
        super(TravelJournalResources.HOME_TITLE);
        PageTracker.set(() -> this);
    }

    @Override
    protected void init() {
        super.init();
        var yo = 45;
        var lineHeight = 24;

        for (var button : TravelJournalClient.HOME_BUTTONS) {
            addRenderableWidget(button.apply(midX, yo));
            yo += lineHeight;
        }

        addRenderableWidget(new CloseButton(midX - (CloseButton.WIDTH / 2),220, b -> onClose()));
        initShortcuts();
    }
}

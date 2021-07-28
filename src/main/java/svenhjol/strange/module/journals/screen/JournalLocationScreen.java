package svenhjol.strange.module.journals.screen;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.data.JournalLocation;

public class JournalLocationScreen extends BaseJournalScreen {
    public JournalLocationScreen(JournalLocation location) {
        super(new TextComponent(location.getName()));

        // add a back button at the bottom
        bottomButtons.add(0, new ButtonDefinition(b -> reopenLocations(),
            new TranslatableComponent("gui.strange.journal.go_back")));
    }

    /**
     * We need to resync the journal when leaving this page to go back to locations.
     */
    protected void reopenLocations() {
        JournalsClient.sendOpenJournal(Journals.Page.LOCATIONS);
    }
}

package svenhjol.strange.module.journals.screen.knowledge;

import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.paginator.ResourcePaginator;
import svenhjol.strange.module.journals2.paginator.StructurePaginator;

import java.util.List;

public class JournalStructuresScreen extends JournalResourcesScreen {
    public JournalStructuresScreen() {
        super(LEARNED_STRUCTURES);
    }

    @Override
    protected ResourcePaginator getPaginator() {
        var journal = Journals2Client.journal;
        return new StructurePaginator(journal != null ? journal.getLearnedStructures() : List.of());
    }

    @Override
    protected void setViewedPage() {
        Journals2Client.tracker.setPage(Journals.Page.STRUCTURES, offset);
    }
}

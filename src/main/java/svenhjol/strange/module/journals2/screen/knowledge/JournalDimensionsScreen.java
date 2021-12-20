package svenhjol.strange.module.journals2.screen.knowledge;

import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;
import svenhjol.strange.module.journals2.paginator.DimensionPaginator;
import svenhjol.strange.module.journals2.paginator.ResourcePaginator;

import java.util.List;

public class JournalDimensionsScreen extends JournalResourcesScreen {
    public JournalDimensionsScreen() {
        super(LEARNED_DIMENSIONS);
    }

    @Override
    protected ResourcePaginator getPaginator() {
        var journal = Journals2Client.journal;
        return new DimensionPaginator(journal != null ? journal.getLearnedDimensions() : List.of());
    }

    @Override
    protected void setViewedPage() {
        Journals2Client.tracker.setPage(PageTracker.Page.DIMENSIONS, offset);
    }
}

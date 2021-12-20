package svenhjol.strange.module.journals2.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;
import svenhjol.strange.module.journals2.paginator.DimensionPaginator;
import svenhjol.strange.module.journals2.paginator.ResourcePaginator;

import java.util.List;
import java.util.function.Consumer;

public class JournalDimensionsScreen extends JournalResourcesScreen<ResourceLocation> {
    public JournalDimensionsScreen() {
        super(LEARNED_DIMENSIONS);
    }

    @Override
    protected Consumer<ResourceLocation> onClick() {
        return dimension -> minecraft.setScreen(new JournalDimensionScreen(dimension));
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

package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.journals.paginator.DimensionPaginator;
import svenhjol.strange.module.journals.paginator.ResourcePaginator;

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
        var journal = JournalsClient.journal;
        return new DimensionPaginator(journal != null ? journal.getLearnedDimensions() : List.of());
    }

    @Override
    protected void setViewedPage() {
        JournalsClient.tracker.setPage(PageTracker.Page.DIMENSIONS, offset);
    }
}

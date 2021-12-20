package svenhjol.strange.module.journals2.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;
import svenhjol.strange.module.journals2.paginator.StructurePaginator;

import java.util.List;
import java.util.function.Consumer;

public class JournalStructuresScreen extends JournalResourcesScreen<ResourceLocation> {
    public JournalStructuresScreen() {
        super(LEARNED_STRUCTURES);
    }

    @Override
    protected Consumer<ResourceLocation> onClick() {
        return structure -> minecraft.setScreen(new JournalStructureScreen(structure));
    }

    @Override
    protected StructurePaginator getPaginator() {
        var journal = Journals2Client.journal;
        return new StructurePaginator(journal != null ? journal.getLearnedStructures() : List.of());
    }

    @Override
    protected void setViewedPage() {
        Journals2Client.tracker.setPage(PageTracker.Page.STRUCTURES, offset);
    }
}

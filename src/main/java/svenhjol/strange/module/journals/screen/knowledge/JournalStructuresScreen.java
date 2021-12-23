package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.journals.paginator.StructurePaginator;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
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
        var journal = JournalsClient.getJournal().orElse(null);
        return new StructurePaginator(journal != null ? journal.getLearnedStructures() : List.of());
    }

    @Override
    protected void setViewedPage() {
        JournalsClient.tracker.setPage(PageTracker.Page.STRUCTURES, offset);
    }
}

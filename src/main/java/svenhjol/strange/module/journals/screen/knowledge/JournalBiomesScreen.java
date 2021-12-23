package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.journals.paginator.BiomePaginator;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class JournalBiomesScreen extends JournalResourcesScreen<ResourceLocation> {
    public JournalBiomesScreen() {
        super(LEARNED_BIOMES);
    }

    @Override
    protected Consumer<ResourceLocation> onClick() {
        return biome -> minecraft.setScreen(new JournalBiomeScreen(biome));
    }

    @Override
    protected BiomePaginator getPaginator() {
        var journal = JournalsClient.getJournal().orElse(null);
        return new BiomePaginator(journal != null ? journal.getLearnedBiomes() : List.of());
    }

    @Override
    protected void setViewedPage() {
        JournalsClient.tracker.setPage(PageTracker.Page.BIOMES, offset);
    }
}

package svenhjol.strange.module.journals2.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;
import svenhjol.strange.module.journals2.paginator.BiomePaginator;

import java.util.List;
import java.util.function.Consumer;

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
        var journal = Journals2Client.journal;
        return new BiomePaginator(journal != null ? journal.getLearnedBiomes() : List.of());
    }

    @Override
    protected void setViewedPage() {
        Journals2Client.tracker.setPage(PageTracker.Page.BIOMES, offset);
    }
}

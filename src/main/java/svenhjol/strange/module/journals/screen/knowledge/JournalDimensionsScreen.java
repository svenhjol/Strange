package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.JournalViewer;
import svenhjol.strange.module.journals.Journals;

import java.util.List;
import java.util.function.Supplier;

public class JournalDimensionsScreen extends JournalResourcesScreen {
    public JournalDimensionsScreen() {
        super(LEARNED_DIMENSIONS);
    }

    @Override
    protected List<ResourceLocation> getResources(JournalData journal) {
        return journal.getLearnedDimensions();
    }

    @Override
    protected Supplier<Component> getLabelForNoItem() {
        return () -> NO_DIMENSIONS;
    }

    @Override
    protected void select(ResourceLocation dimension) {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalDimensionScreen(dimension)));
    }

    @Override
    protected void setViewedPage() {
        JournalViewer.viewedPage(Journals.Page.DIMENSIONS, lastPage);
    }
}

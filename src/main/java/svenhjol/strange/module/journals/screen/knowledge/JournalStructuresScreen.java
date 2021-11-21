package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.JournalViewer;
import svenhjol.strange.module.journals.Journals;

import java.util.List;
import java.util.function.Supplier;

public class JournalStructuresScreen extends JournalResourcesScreen {
    public JournalStructuresScreen() {
        super(LEARNED_STRUCTURES);
    }

    @Override
    protected List<ResourceLocation> getResources(JournalData journal) {
        return journal.getLearnedStructures();
    }

    @Override
    protected Supplier<Component> getLabelForNoItem() {
        return () -> NO_STRUCTURES;
    }

    @Override
    protected void select(ResourceLocation structure) {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalStructureScreen(structure)));
    }

    @Override
    protected void setViewedPage() {
        JournalViewer.viewedPage(Journals.Page.STRUCTURES, lastPage);
    }
}

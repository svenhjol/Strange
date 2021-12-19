package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.knowledge2.Knowledge2Client;
import svenhjol.strange.module.runes.RuneBranch;

import javax.annotation.Nullable;

public class JournalStructureScreen extends JournalResourceScreen {
    public JournalStructureScreen(ResourceLocation structure) {
        super(structure);
    }

    @Override
    protected void init() {
        super.init();
        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> structures(), GO_BACK));
    }

    @Nullable
    @Override
    public RuneBranch<?, ResourceLocation> getBranch() {
        return Knowledge2Client.structures;
    }

    @Override
    protected void setViewedPage() {
        Journals2Client.tracker.setResource(Journals.Page.STRUCTURE, resource);
    }
}

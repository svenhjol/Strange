package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.knowledge2.Knowledge2Client;
import svenhjol.strange.module.runes.RuneBranch;

import javax.annotation.Nullable;

public class JournalDimensionScreen extends JournalResourceScreen {
    public JournalDimensionScreen(ResourceLocation dimension) {
        super(dimension);
    }

    @Override
    protected void init() {
        super.init();
        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> dimensions(), GO_BACK));
    }

    @Nullable
    @Override
    public RuneBranch<?, ResourceLocation> getBranch() {
        return Knowledge2Client.dimensions;
    }

    @Override
    protected void setViewedPage() {
        JournalsClient.tracker.setResource(PageTracker.Page.DIMENSION, resource);
    }
}

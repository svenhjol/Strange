package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;
import svenhjol.strange.module.knowledge2.Knowledge2Client;
import svenhjol.strange.module.runes.RuneBranch;

import javax.annotation.Nullable;

public class JournalBiomeScreen extends JournalResourceScreen {
    public JournalBiomeScreen(ResourceLocation biome) {
        super(biome);
    }

    @Override
    protected void init() {
        super.init();
        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> biomes(), GO_BACK));
    }

    @Nullable
    @Override
    public RuneBranch<?, ResourceLocation> getBranch() {
        return Knowledge2Client.biomes;
    }

    @Override
    protected void setViewedPage() {
        Journals2Client.tracker.setResource(PageTracker.Page.BIOME, resource);
    }
}

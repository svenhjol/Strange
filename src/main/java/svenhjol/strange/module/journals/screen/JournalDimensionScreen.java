package svenhjol.strange.module.journals.screen;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeData;

public class JournalDimensionScreen extends JournalResourceScreen {
    public JournalDimensionScreen(ResourceLocation dimension) {
        super(dimension);

        // add a back button at the bottom
        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> dimensions(), GO_BACK));
    }

    @Override
    public KnowledgeBranch<?, ResourceLocation> getBranch(KnowledgeData knowledge) {
        return knowledge.dimensions;
    }
}

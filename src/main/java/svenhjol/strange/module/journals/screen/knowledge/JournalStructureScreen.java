package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeData;

public class JournalStructureScreen extends JournalResourceScreen {
    public JournalStructureScreen(ResourceLocation structure) {
        super(structure);

        // add a back button at the bottom
        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> structures(), GO_BACK));
    }

    @Override
    public KnowledgeBranch<?, ResourceLocation> getBranch(KnowledgeData knowledge) {
        return knowledge.structures;
    }
}

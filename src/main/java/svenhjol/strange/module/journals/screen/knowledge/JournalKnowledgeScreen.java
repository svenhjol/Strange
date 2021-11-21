package svenhjol.strange.module.journals.screen.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalViewer;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.screen.JournalScreen;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class JournalKnowledgeScreen extends JournalScreen {
    protected List<GuiHelper.ButtonDefinition> knowledgeButtons;

    public JournalKnowledgeScreen() {
        this(KNOWLEDGE);
    }

    public JournalKnowledgeScreen(Component component) {
        super(component);

        this.knowledgeButtons = Arrays.asList(
            new GuiHelper.ButtonDefinition(b -> runes(), LEARNED_RUNES),
            new GuiHelper.ButtonDefinition(b -> biomes(), LEARNED_BIOMES),
            new GuiHelper.ButtonDefinition(b -> structures(), LEARNED_STRUCTURES),
            new GuiHelper.ButtonDefinition(b -> dimensions(), LEARNED_DIMENSIONS)
        );
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        renderKnowledgeButtons(poseStack);
        JournalViewer.viewedPage(Journals.Page.KNOWLEDGE);
    }

    public void renderKnowledgeButtons(PoseStack poseStack) {
        if (!hasRenderedButtons) {
            int buttonWidth = 105;
            int buttonHeight = 20;
            int x = midX - 50;
            int y = 40;
            int yOffset = 24;

            GuiHelper.renderButtons(this, width, font, knowledgeButtons, x, y, 0, yOffset, buttonWidth, buttonHeight);
            hasRenderedButtons = true;
        }
    }
}

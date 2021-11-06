package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import svenhjol.strange.helper.GuiHelper;

import java.util.Arrays;
import java.util.List;

public class JournalKnowledgeScreen extends JournalScreen {
    protected boolean hasRenderedButtons;
    protected List<GuiHelper.ButtonDefinition> knowledgeButtons;

    public JournalKnowledgeScreen() {
        this(KNOWLEDGE);
    }

    public JournalKnowledgeScreen(Component component) {
        super(component);

        this.knowledgeButtons = Arrays.asList(
            new GuiHelper.ButtonDefinition(b -> runes(), LEARNED_RUNES),
            new GuiHelper.ButtonDefinition(b -> biomes(), LEARNED_BIOMES),
            new GuiHelper.ButtonDefinition(b -> onClose(), LEARNED_STRUCTURES),
            new GuiHelper.ButtonDefinition(b -> onClose(), LEARNED_DIMENSIONS)
        );

        this.hasRenderedButtons = false;
    }

    @Override
    protected void init() {
        super.init();
        hasRenderedButtons = false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        renderKnowledgeButtons(poseStack);
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, titleX, titleY, titleColor);
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

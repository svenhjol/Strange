package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.helper.GuiHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JournalHomeScreen extends BaseJournalScreen {
    protected boolean hasRenderedHomeButtons = false;
    protected List<GuiHelper.ButtonDefinition> homeButtons = new ArrayList<>();

    public JournalHomeScreen() {
        super(new TranslatableComponent("gui.strange.journal.title"));

        homeButtons.addAll(Arrays.asList(
            new GuiHelper.ButtonDefinition(b -> locations(),
                new TranslatableComponent("gui.strange.journal.locations")),

            new GuiHelper.ButtonDefinition(b -> quests(),
                new TranslatableComponent("gui.strange.journal.quests")),

            new GuiHelper.ButtonDefinition(b -> knowledge(),
                new TranslatableComponent("gui.strange.journal.knowledge"))
        ));
    }

    @Override
    protected void init() {
        super.init();
        hasRenderedHomeButtons = false;
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return COVER_BACKGROUND;
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        // center the title for the home screen
        super.renderTitle(poseStack, titleX, 25, titleColor);
    }

    @Override
    public void renderNavigation(PoseStack poseStack) {
        // don't render navigation on home screen
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        renderButtons(poseStack);
    }

    public void renderButtons(PoseStack poseStack) {
        if (!hasRenderedHomeButtons) {
            int buttonWidth = 100;
            int buttonHeight = 20;
            int x = (width / 2) - (buttonWidth / 2);
            int y = 48;
            int yOffset = 24;

            GuiHelper.renderButtons(this, width, font, homeButtons, x, y, 0, yOffset, buttonWidth, buttonHeight);
            hasRenderedHomeButtons = true;
        }
    }
}

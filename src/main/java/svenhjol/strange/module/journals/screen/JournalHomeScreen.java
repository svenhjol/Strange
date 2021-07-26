package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class JournalHomeScreen extends BaseJournalScreen {
    public boolean hasRenderedButtons = false;

    public JournalHomeScreen() {
        super(new TranslatableComponent("gui.strange.journal.title"));
    }

    @Override
    protected void init() {
        super.init();
        hasRenderedButtons = false;
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return COVER_BACKGROUND;
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        // center the title for the home screen
        super.renderTitle(poseStack, 0, titleY, titleColor);
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
        int buttonWidth = 100;
        int buttonHeight = 20;
        int x = (width / 2) - (buttonWidth / 2);
        int y = 48;
        int yOffset = 24;

        if (!hasRenderedButtons) {
            // render the buttons on the journal itself
            y += yOffset;
            addRenderableWidget(new Button(x, y, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.journal.locations"), button
                -> locations()));

            y += yOffset;
            addRenderableWidget(new Button(x, y, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.journal.quests"), button
                -> quests()));

            y += yOffset;
            addRenderableWidget(new Button(x, y, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.journal.knowledge"), button
                -> knowledge()));

            // render the buttons below the journal
            y = (height / 4) + 140;
            addRenderableWidget(new Button(x, y, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.journal.close"), button
                -> onClose()));

            hasRenderedButtons = true;
        }
    }
}

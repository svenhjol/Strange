package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;

public class JournalHomeScreen extends Screen {
    public static final int BGWIDTH = 256;
    public static final int BGHEIGHT = 192;
    public static final ResourceLocation BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_cover.png");

    public boolean hasRenderedButtons = false;

    public JournalHomeScreen() {
        super(new TranslatableComponent("gui.strange.journal.title"));
        passEvents = true;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderTitle(poseStack);
        renderButtons(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();

        hasRenderedButtons = false;
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);

        ClientHelper.getClient().ifPresent(client -> {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, BACKGROUND);
            int mid = (width - BGWIDTH) / 2;
            blit(poseStack, mid, 2, 0, 0, BGWIDTH, BGHEIGHT);
        });
    }

    public void renderTitle(PoseStack poseStack) {
        int mid = width / 2;
        int yOffset = 25;
        int color = 0x000000;
        centeredString(poseStack, font, I18n.get("gui.strange.journal.title"), mid, yOffset, color);
    }

    public void renderButtons(PoseStack poseStack) {
        int buttonWidth = 100;
        int buttonHeight = 20;

        if (!hasRenderedButtons) {
            int yOffset = 62;
            int rowHeight = 24;

            yOffset += rowHeight;
            addRenderableWidget(new Button((width / 2) - (buttonWidth / 2), yOffset, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.journal.saved_locations"),
                button -> onClose()));

            hasRenderedButtons = true;
        }

        addRenderableWidget(new Button((width / 2) - (buttonWidth / 2), (height / 4) + 140, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.journal.close"),
            button -> onClose()));
    }

    protected void centeredString(PoseStack poseStack, Font renderer, String string, int x, int y, int color) {
        renderer.draw(poseStack, string, x - (float)(renderer.width(string) / 2), y, color);
    }

    public static void centeredText(PoseStack poseStack, Font renderer, Component text, int x, int y, int color) {
        FormattedCharSequence orderedText = text.getVisualOrderText();
        renderer.draw(poseStack, orderedText, (float)(x - renderer.width(orderedText) / 2), (float)y, color);
    }
}

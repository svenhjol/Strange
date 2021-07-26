package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;

public abstract class BaseJournalScreen extends Screen {
    public static final int BGWIDTH = 256;
    public static final int BGHEIGHT = 192;

    public static final ResourceLocation COVER_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_cover.png");
    public static final ResourceLocation OPEN_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_open.png");
    public static final ResourceLocation NAVIGATION = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_navigation.png");

    protected boolean hasRenderedNavigation = false;

    protected int navigationX = -126;
    protected int navigationY = 18;
    protected int navigationYOffset = 18;

    protected int titleX = -60;
    protected int titleY = 25;
    protected int titleColor = 0x000000;

    protected BaseJournalScreen(Component component) {
        super(component);
        passEvents = true;
    }

    @Override
    protected void init() {
        super.init();
        hasRenderedNavigation = false;
    }

    protected ResourceLocation getBackgroundTexture() {
        return OPEN_BACKGROUND;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderTitle(poseStack, titleX, titleY, titleColor);
        renderNavigation(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);

        ClientHelper.getClient().ifPresent(client -> {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, getBackgroundTexture());
            int mid = (width - BGWIDTH) / 2;
            blit(poseStack, mid, 2, 0, 0, BGWIDTH, BGHEIGHT);
        });
    }

    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        centeredString(poseStack, font, getTitle(), (width / 2) + titleX, titleY, titleColor);
    }

    public void renderNavigation(PoseStack poseStack) {
        int mid = this.width / 2;
        int top = navigationY;

        if (!hasRenderedNavigation) {
            // button to open home page
            this.addRenderableWidget(new ImageButton(mid + navigationX, top, 20, 18, 0, 37, 19, NAVIGATION, button -> home()));
            top += navigationYOffset;

            // button to open locations page
            this.addRenderableWidget(new ImageButton(mid + navigationX, top, 20, 18, 60, 37, 19, NAVIGATION, button -> locations()));
            top += navigationYOffset;

            // button to open quests page
            this.addRenderableWidget(new ImageButton(mid + navigationX, top, 20, 18, 20, 37, 19, NAVIGATION, button -> quests()));
            top += navigationYOffset;

            // button to open knowledge page
            this.addRenderableWidget(new ImageButton(mid + navigationX, top, 20, 18, 40, 37, 19, NAVIGATION, button -> knowledge()));
            top += navigationYOffset;

            hasRenderedNavigation = true;
        }
    }

    protected void home() {
        ClientHelper.getClient().ifPresent(client
            -> client.setScreen(new JournalHomeScreen()));
    }

    protected void knowledge() {
        ClientHelper.getClient().ifPresent(client
            -> client.setScreen(new JournalKnowledgeScreen()));
    }

    protected void locations() {
        ClientHelper.getClient().ifPresent(client
            -> client.setScreen(new JournalLocationsScreen()));
    }

    protected void quests() {
        ClientHelper.getClient().ifPresent(client
            -> client.setScreen(new JournalQuestsScreen()));
    }

    protected void centeredString(PoseStack poseStack, Font renderer, Component component, int x, int y, int color) {
        String string = component.getString();
        renderer.draw(poseStack, string, x - (float)(renderer.width(string) / 2), y, color);
    }

    public static void centeredText(PoseStack poseStack, Font renderer, Component text, int x, int y, int color) {
        FormattedCharSequence orderedText = text.getVisualOrderText();
        renderer.draw(poseStack, orderedText, (float)(x - renderer.width(orderedText) / 2), (float)y, color);
    }
}

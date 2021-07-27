package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseJournalScreen extends Screen {
    public static final int BGWIDTH = 256;
    public static final int BGHEIGHT = 192;

    public static final ResourceLocation COVER_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_cover.png");
    public static final ResourceLocation OPEN_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_open.png");
    public static final ResourceLocation NAVIGATION = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_navigation.png");
    public static final ResourceLocation SGA_TEXTURE = new ResourceLocation("minecraft", "alt");

    protected final Style SGA_STYLE = Style.EMPTY.withFont(SGA_TEXTURE);

    protected boolean hasRenderedBottomButtons = false;
    protected boolean hasRenderedNavigation = false;

    protected int navigationX = -130;
    protected int navigationY = 18;
    protected int navigationYOffset = 18;

    protected int titleX = -53;
    protected int titleY = 25;

    protected int textColor = 0x000000;
    protected int titleColor = 0x000000;
    protected int errorColor = 0x770000;

    protected int page1TitleX = -53;
    protected int page2TitleX = 53;

    protected List<ButtonDefinition> bottomButtons = new ArrayList<>();
    protected List<ImageButtonDefinition> navigationButtons = new ArrayList<>();

    protected BaseJournalScreen(Component component) {
        super(component);
        passEvents = true;

        bottomButtons.add(
            new ButtonDefinition(b -> onClose(),
                new TranslatableComponent("gui.strange.journal.close"))
        );

        navigationButtons.addAll(Arrays.asList(
            new ImageButtonDefinition(b -> home(), NAVIGATION, 0, 36, 18,
                new TranslatableComponent("gui.strange.journal.home_tooltip")),
            new ImageButtonDefinition(b -> locations(), NAVIGATION, 60, 36, 18,
                new TranslatableComponent("gui.strange.journal.locations_tooltip")),
            new ImageButtonDefinition(b -> quests(), NAVIGATION, 20, 36, 18,
                new TranslatableComponent("gui.strange.journal.quests_tooltip")),
            new ImageButtonDefinition(b -> knowledge(), NAVIGATION, 40, 36, 18,
                new TranslatableComponent("gui.strange.journal.knowledge_tooltip"))
        ));
    }

    @Override
    protected void init() {
        super.init();
        hasRenderedNavigation = false;
        hasRenderedBottomButtons = false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderTitle(poseStack, titleX, titleY, titleColor);
        renderNavigation(poseStack);
        renderBottomButtons(poseStack);
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

    /**
     * Override this to return different backgrounds in subclasses.
     */
    protected ResourceLocation getBackgroundTexture() {
        return OPEN_BACKGROUND;
    }

    /**
     * Override this to change title color and position in subclasses.
     */
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        centeredString(poseStack, font, getTitle(), (width / 2) + titleX, titleY, titleColor);
    }

    public void renderNavigation(PoseStack poseStack) {
        int x = (this.width / 2) + navigationX;
        int y = navigationY;
        int buttonWidth = 20;
        int buttonHeight = 18;

        if (!hasRenderedNavigation) {
            renderImageButtons(navigationButtons, x, y, 0, navigationYOffset, buttonWidth, buttonHeight);
            hasRenderedNavigation = true;
        }
    }

    public void renderBottomButtons(PoseStack poseStack) {
        if (!hasRenderedBottomButtons) {
            int buttonWidth = 100;
            int buttonHeight = 20;
            int xOffset = 102;

            int numberOfButtons = bottomButtons.size();

            int x = (width / 2) - ((numberOfButtons * xOffset) / 2);
            int y = (height / 4) + 140;

            renderButtons(bottomButtons, x, y, xOffset, 0, buttonWidth, buttonHeight);
            hasRenderedBottomButtons = true;
        }
    }

    protected void renderButtons(List<ButtonDefinition> buttons, int x, int y, int xOffset, int yOffset, int buttonWidth, int buttonHeight) {
        for (ButtonDefinition b : buttons) {
            Button.OnTooltip tooltip = b.tooltip != null ? (button, p, tx, ty) -> renderTooltip(p, font.split(b.tooltip, Math.max(width / 2 - 43, 170)), tx, ty) : (button, p, tx, ty) -> {};
            addRenderableWidget(new Button(x, y, buttonWidth, buttonHeight, b.name, b.action, tooltip));

            x += xOffset;
            y += yOffset;
        }
    }

    protected void renderImageButtons(List<ImageButtonDefinition> buttons, int x, int y, int xOffset, int yOffset, int buttonWidth, int buttonHeight) {
        for (ImageButtonDefinition b : buttons) {
            Button.OnTooltip tooltip = b.tooltip != null ? (button, p, tx, ty) -> renderTooltip(p, font.split(b.tooltip, Math.max(width / 2 - 43, 170)), tx, ty) : (button, p, tx, ty) -> {};
            addRenderableWidget(new ImageButton(x, y, buttonWidth, buttonHeight, b.texX, b.texY, b.texHoverOffset, b.texture, 256, 256, b.action, tooltip, TextComponent.EMPTY));

            x += xOffset;
            y += yOffset;
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

    protected static void centeredText(PoseStack poseStack, Font renderer, Component text, int x, int y, int color) {
        FormattedCharSequence orderedText = text.getVisualOrderText();
        renderer.draw(poseStack, orderedText, (float)(x - renderer.width(orderedText) / 2), (float)y, color);
    }

    protected static class ButtonDefinition {
        private final Component name;
        private final Component tooltip;
        private final Button.OnPress action;

        public ButtonDefinition(Button.OnPress action, @Nullable Component name) {
            this(action, name, null);
        }

        public ButtonDefinition(Button.OnPress action, @Nullable Component name, @Nullable Component tooltip) {
            this.name = name;
            this.action = action;
            this.tooltip = tooltip;
        }
    }

    protected static class ImageButtonDefinition {
        private final Button.OnPress action;
        private final Component tooltip;
        private final ResourceLocation texture;
        private final int texX;
        private final int texY;
        private final int texHoverOffset;

        public ImageButtonDefinition(Button.OnPress action, ResourceLocation texture, int texX, int texY, int texHoverOffset) {
            this(action, texture, texX, texY, texHoverOffset, null);
        }

        public ImageButtonDefinition(Button.OnPress action, ResourceLocation texture, int texX, int texY, int texHoverOffset, @Nullable Component tooltip) {
            this.action = action;
            this.tooltip = tooltip;
            this.texture = texture;
            this.texX = texX;
            this.texY = texY;
            this.texHoverOffset = texHoverOffset;
        }
    }
}

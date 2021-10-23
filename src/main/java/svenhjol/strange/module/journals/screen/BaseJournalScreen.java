package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseJournalScreen extends Screen {
    public static final int BGWIDTH = 256;
    public static final int BGHEIGHT = 208;
    public static final int KNOWN_COLOR = 0x707070;
    public static final int UNKNOWN_COLOR = 0xd0c0c0;

    public static final ResourceLocation COVER_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_cover.png");
    public static final ResourceLocation OPEN_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_open.png");
    public static final ResourceLocation NAVIGATION = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_navigation.png");
    public static final ResourceLocation DEFAULT_GLYPHS = new ResourceLocation("minecraft", "alt");
    public static final ResourceLocation ILLAGER_GLYPHS = new ResourceLocation("minecraft", "illageralt");

    protected final Style DEFAULT_GLYPHS_STYLE = Style.EMPTY.withFont(DEFAULT_GLYPHS);
    protected final Style ILLAGER_GLYPHS_STYLE = Style.EMPTY.withFont(ILLAGER_GLYPHS);

    protected boolean hasRenderedBottomButtons = false;
    protected boolean hasRenderedNavigation = false;

    protected int navigationX = -141; // navigation left relative to center
    protected int navigationY = 18; // navigation top
    protected int navigationYOffset = 18;

    protected int bottomButtonsY = 158;

    protected int titleX = 0;
    protected int titleY = 22;
    protected int page1Center = -56;
    protected int page2Center = 56;

    protected int textColor = 0x000000;
    protected int secondaryColor = 0x908080;
    protected int titleColor = 0x000000;
    protected int errorColor = 0x770000;


    protected List<GuiHelper.ButtonDefinition> bottomButtons = new ArrayList<>();
    protected List<GuiHelper.ImageButtonDefinition> navigationButtons = new ArrayList<>();

    public BaseJournalScreen(Component component) {
        super(component);
        passEvents = false;

        bottomButtons.add(
            new GuiHelper.ButtonDefinition(b -> onClose(),
                new TranslatableComponent("gui.strange.journal.close"))
        );

        navigationButtons.addAll(Arrays.asList(
            new GuiHelper.ImageButtonDefinition(b -> home(), NAVIGATION, 0, 36, 18,
                new TranslatableComponent("gui.strange.journal.home_tooltip")),
            new GuiHelper.ImageButtonDefinition(b -> locations(), NAVIGATION, 60, 36, 18,
                new TranslatableComponent("gui.strange.journal.locations_tooltip")),
            new GuiHelper.ImageButtonDefinition(b -> quests(), NAVIGATION, 20, 36, 18,
                new TranslatableComponent("gui.strange.journal.quests_tooltip")),
            new GuiHelper.ImageButtonDefinition(b -> knowledge(), NAVIGATION, 40, 36, 18,
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
        centeredText(poseStack, font, getTitle(), (width / 2) + titleX, titleY, titleColor);
    }

    public void renderTitleIcon(ItemStack icon) {
        // render icon next to title
        int iconX = width / 2 - 17 - ((this.title.getString().length() * 6) / 2);
        itemRenderer.renderGuiItem(icon, iconX, titleY - 5);
    }

    public void renderNavigation(PoseStack poseStack) {
        int x = (this.width / 2) + navigationX;
        int y = navigationY;
        int buttonWidth = 20;
        int buttonHeight = 18;

        if (!hasRenderedNavigation) {
            GuiHelper.renderImageButtons(this, width, font, navigationButtons, x, y, 0, navigationYOffset, buttonWidth, buttonHeight);
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
            int y = (height / 4) + bottomButtonsY;

            GuiHelper.renderButtons(this, width, font, bottomButtons, x, y, xOffset, 0, buttonWidth, buttonHeight);
            hasRenderedBottomButtons = true;
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

    protected void centeredText(PoseStack poseStack, Font renderer, Component component, int x, int y, int color) {
        String string = component.getString();
        renderer.draw(poseStack, string, x - (float)(renderer.width(string) / 2), y, color);
    }

}

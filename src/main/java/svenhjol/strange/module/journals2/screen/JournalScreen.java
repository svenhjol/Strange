package svenhjol.strange.module.journals2.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.helper.GuiHelper.ButtonDefinition;
import svenhjol.strange.helper.GuiHelper.ImageButtonDefinition;
import svenhjol.strange.module.journals2.screen.bookmark.JournalBookmarksScreen;
import svenhjol.strange.module.journals2.screen.knowledge.*;
import svenhjol.strange.module.journals2.screen.quest.JournalQuestsScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"SameParameterValue", "ConstantConditions"})
public abstract class JournalScreen extends Screen {
    public static final ResourceLocation COVER_BACKGROUND;
    public static final ResourceLocation OPEN_BACKGROUND;
    public static final ResourceLocation NAVIGATION;

    public static final Component ADD_BOOKMARK;
    public static final Component CHOOSE_ICON;
    public static final Component CLOSE;
    public static final Component DELETE_TOOLTIP;
    public static final Component GO_BACK;
    public static final Component HOME_TOOLTIP;
    public static final Component JOURNAL;
    public static final Component KNOWLEDGE;
    public static final Component KNOWLEDGE_TOOLTIP;
    public static final Component LEARNED_BIOMES;
    public static final Component LEARNED_DIMENSIONS;
    public static final Component LEARNED_RUNES;
    public static final Component LEARNED_STRUCTURES;
    public static final Component BOOKMARKS;
    public static final Component BOOKMARKS_TOOLTIP;
    public static final Component MAKE_MAP_TOOLTIP;
    public static final Component NO_BIOMES;
    public static final Component NO_DIMENSIONS;
    public static final Component NO_BOOKMARKS;
    public static final Component NO_STRUCTURES;
    public static final Component NO_QUESTS;
    public static final Component QUESTS;
    public static final Component QUESTS_TOOLTIP;
    public static final Component SAVE;
    public static final Component TAKE_PHOTO;
    public static final Component TAKE_PHOTO_TOOLTIP;

    protected int backgroundWidth;
    protected int backgroundHeight;

    protected int leftNavX; // navigation left relative to center
    protected int leftNavY;
    protected int leftNavYOffset;

    protected int rightNavX; // navigation right relative to center
    protected int rightNavY;
    protected int rightNavYOffset;

    protected int bottomNavX;
    protected int bottomNavY;
    protected int bottomNavYOffset;

    protected int bottomButtonsY;

    protected int titleX;
    protected int titleY;
    protected int page1Center;
    protected int page2Center;

    protected int midX;
    protected int midY;

    protected int textColor;
    protected int secondaryColor;
    protected int titleColor;
    protected int errorColor;
    protected int subheadingColor;
    protected int completedColor;

    protected boolean hasFirstRendered;
    protected int offset;

    protected List<ImageButtonDefinition> leftNavButtons = new ArrayList<>();
    protected List<ImageButtonDefinition> rightNavButtons = new ArrayList<>();
    protected List<ImageButtonDefinition> bottomNavButtons = new ArrayList<>();
    protected List<ButtonDefinition> bottomButtons = new ArrayList<>();

    public JournalScreen(Component component) {
        super(component);

        this.passEvents = false;
        this.offset = 0;

        this.backgroundWidth = 256;
        this.backgroundHeight = 208;

        this.titleX = 0;
        this.titleY = 22;

        // left navigation renders downwards from leftNavY
        this.leftNavX = -141;
        this.leftNavY = 18;
        this.leftNavYOffset = 18;

        // bottom navigation renders upwards from bottomNavY
        this.bottomNavX = -141;
        this.bottomNavY = 180;
        this.bottomNavYOffset = -18;

        // right navigation renders downwards from rightNavY
        this.rightNavX = 119;
        this.rightNavY = 18;
        this.rightNavYOffset = 18;

        this.page1Center = -56;
        this.page2Center = 56;
        this.bottomButtonsY = 158;

        this.textColor = 0x000000;
        this.secondaryColor = 0x908080;
        this.titleColor = 0x000000;
        this.errorColor = 0x770000;
        this.subheadingColor = 0x887777;
        this.completedColor = 0x228822;
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null || minecraft.player == null) return;

        midX = width / 2;
        midY = width / 2;

        leftNavButtons.clear();
        leftNavButtons.addAll(Arrays.asList(
            new ImageButtonDefinition(b -> home(), NAVIGATION, 0, 36, 18, HOME_TOOLTIP),
            new ImageButtonDefinition(b -> bookmarks(), NAVIGATION, 60, 36, 18, BOOKMARKS_TOOLTIP),
            new ImageButtonDefinition(b -> quests(), NAVIGATION, 20, 36, 18, QUESTS_TOOLTIP),
            new ImageButtonDefinition(b -> knowledge(), NAVIGATION, 40, 36, 18, KNOWLEDGE_TOOLTIP)
        ));

        rightNavButtons.clear();
        bottomNavButtons.clear();

        bottomButtons.clear();
        bottomButtons.addAll(List.of(
            new ButtonDefinition(b -> onClose(), CLOSE)
        ));

        hasFirstRendered = false;
    }

    protected void firstRender(PoseStack poseStack) {
        if (!hasFirstRendered) {
            renderNavigation();
            renderBottomButtons();
            hasFirstRendered = true;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        firstRender(poseStack);
        renderBackground(poseStack);
        renderTitle(poseStack, titleX, titleY, titleColor);
        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, getBackgroundTexture());
        int mid = (width - backgroundWidth) / 2;
        blit(poseStack, mid, 2, 0, 0, backgroundWidth, backgroundHeight);
    }

    /**
     * Override this to return a custom background.
     */
    protected ResourceLocation getBackgroundTexture() {
        return OPEN_BACKGROUND;
    }

    /**
     * Override this to change the position and color of the title.
     */
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        GuiHelper.drawCenteredString(poseStack, font, getTitle(), midX + titleX, titleY, titleColor);
    }

    /**
     * Renders an item to the left of the main title.
     */
    public void renderTitleIcon(ItemStack icon) {
        int iconX = midX - 21 - ((this.title.getString().length() * 6) / 2);
        itemRenderer.renderGuiItem(icon, iconX, titleY - 5);
    }

    protected void renderNavigation() {
        int x;
        int y;
        int navButtonWidth = 20;
        int navButtonHeight = 18;

        // render left buttons
        x = midX + leftNavX;
        y = leftNavY;
        GuiHelper.addImageButtons(this, width, font, leftNavButtons, x, y, 0, leftNavYOffset, navButtonWidth, navButtonHeight);

        // render bottom buttons
        x = midX + bottomNavX;
        y = bottomNavY;
        GuiHelper.addImageButtons(this, width, font, bottomNavButtons, x, y, 0, bottomNavYOffset, navButtonWidth, navButtonHeight);

        // render right buttons
        x = midX + rightNavX;
        y = rightNavY;
        GuiHelper.addImageButtons(this, width, font, rightNavButtons, x, y, 0, rightNavYOffset, navButtonWidth, navButtonHeight);
    }

    protected void renderBottomButtons() {
        int bottomButtonWidth = 100;
        int bottomButtonHeight = 20;
        int xOffset = 102;
        int numberOfButtons = bottomButtons.size();

        int x = midX - ((numberOfButtons * xOffset) / 2);
        int y = (height / 4) + bottomButtonsY;

        GuiHelper.addButtons(this, width, font, bottomButtons, x, y, xOffset, 0, bottomButtonWidth, bottomButtonHeight);
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    protected void home() {
        minecraft.setScreen(new JournalHomeScreen());
    }

    protected void knowledge() {
        minecraft.setScreen(new JournalKnowledgeScreen());
    }

    protected void biomes() {
        minecraft.setScreen(new JournalBiomesScreen());
    }

    protected void runes() {
        minecraft.setScreen(new JournalRunesScreen());
    }

    protected void bookmarks() {
        minecraft.setScreen(new JournalBookmarksScreen());
    }

    protected void structures() {
        minecraft.setScreen(new JournalStructuresScreen());
    }

    protected void dimensions() {
        minecraft.setScreen(new JournalDimensionsScreen());
    }

    protected void quests() {
        minecraft.setScreen(new JournalQuestsScreen());
    }

    protected void centeredText(PoseStack poseStack, Font renderer, Component component, int x, int y, int color) {
        String string = component.getString();
        renderer.draw(poseStack, string, x - (float)(renderer.width(string) / 2), y, color);
    }

    static {
        COVER_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_cover.png");
        OPEN_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_open.png");
        NAVIGATION = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_navigation.png");

        ADD_BOOKMARK = new TranslatableComponent("gui.strange.journal.add_bookmark");
        CHOOSE_ICON = new TranslatableComponent("gui.strange.journal.choose_icon");
        CLOSE = new TranslatableComponent("gui.strange.journal.close");
        DELETE_TOOLTIP = new TranslatableComponent("gui.strange.journal.delete");
        GO_BACK = new TranslatableComponent("gui.strange.journal.go_back");
        HOME_TOOLTIP = new TranslatableComponent("gui.strange.journal.home_tooltip");
        JOURNAL = new TranslatableComponent("gui.strange.journal.title");
        KNOWLEDGE = new TranslatableComponent("gui.strange.journal.knowledge");
        KNOWLEDGE_TOOLTIP = new TranslatableComponent("gui.strange.journal.knowledge_tooltip");
        LEARNED_BIOMES = new TranslatableComponent("gui.strange.journal.learned_biomes");
        LEARNED_DIMENSIONS = new TranslatableComponent("gui.strange.journal.learned_dimensions");
        LEARNED_RUNES = new TranslatableComponent("gui.strange.journal.learned_runes");
        LEARNED_STRUCTURES = new TranslatableComponent("gui.strange.journal.learned_structures");
        BOOKMARKS = new TranslatableComponent("gui.strange.journal.bookmarks");
        BOOKMARKS_TOOLTIP = new TranslatableComponent("gui.strange.journal.bookmarks_tooltip");
        MAKE_MAP_TOOLTIP = new TranslatableComponent("gui.strange.journal.make_map_tooltip");
        NO_QUESTS = new TranslatableComponent("gui.strange.journal.no_quests");
        NO_BIOMES = new TranslatableComponent("gui.strange.journal.no_learned_biomes");
        NO_DIMENSIONS = new TranslatableComponent("gui.strange.journal.no_learned_dimensions");
        NO_BOOKMARKS = new TranslatableComponent("gui.strange.journal.no_bookmarks");
        NO_STRUCTURES = new TranslatableComponent("gui.strange.journal.no_learned_structures");
        QUESTS = new TranslatableComponent("gui.strange.journal.quests");
        QUESTS_TOOLTIP = new TranslatableComponent("gui.strange.journal.quests_tooltip");
        SAVE = new TranslatableComponent("gui.strange.journal.save");
        TAKE_PHOTO = new TranslatableComponent("gui.strange.journal.take_photo");
        TAKE_PHOTO_TOOLTIP = new TranslatableComponent("gui.strange.journal.take_photo_tooltip");
    }
}

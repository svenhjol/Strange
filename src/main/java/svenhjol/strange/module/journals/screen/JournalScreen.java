package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.helper.GuiHelper.ButtonDefinition;
import svenhjol.strange.helper.GuiHelper.ImageButtonDefinition;
import svenhjol.strange.init.StrangeFonts;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarksScreen;
import svenhjol.strange.module.journals.screen.knowledge.*;
import svenhjol.strange.module.journals.screen.quest.JournalQuestsScreen;
import svenhjol.strange.module.journals2.helper.Journal2Helper;
import svenhjol.strange.module.knowledge.KnowledgeHelper;
import svenhjol.strange.module.runes.RuneHelper;

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

    protected boolean hasRenderedBottomButtons;
    protected boolean hasRenderedNavigation;
    protected boolean hasRenderedButtons; // generic buttons used by subclasses

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
    protected int knownColor;
    protected int unknownColor;
    protected int completedColor;

    protected boolean hasFirstRendered = false;

    protected int offset;

    protected List<ImageButtonDefinition> leftNavButtons = new ArrayList<>();
    protected List<ImageButtonDefinition> rightNavButtons = new ArrayList<>();
    protected List<ImageButtonDefinition> bottomNavButtons = new ArrayList<>();
    protected List<ButtonDefinition> bottomButtons = new ArrayList<>();

    public JournalScreen(Component component) {
        super(component);

        this.passEvents = false;
        this.hasRenderedBottomButtons = false;
        this.hasRenderedNavigation = false;
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
        this.unknownColor = 0xd0c0c0;
        this.knownColor = 0x707070;
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

//    protected <T> void paginator(PoseStack poseStack, List<T> items, Consumer<T> renderItem, Supplier<Component> labelForNoItems, boolean shouldRenderButtons) {
//        int perPage = 6;
//        int paginationY = 180;
//        int currentPage = lastPage - 1;
//        List<T> sublist;
//
//        int size = items.size();
//        if (size > perPage) {
//            if (currentPage * perPage >= size || currentPage * perPage < 0) {
//                // out of range, reset
//                lastPage = 1;
//                currentPage = 0;
//            }
//            sublist = items.subList(currentPage * perPage, Math.min(currentPage * perPage + perPage, size));
//        } else {
//            sublist = items;
//        }
//
//        for (T item : sublist) {
//            renderItem.accept(item);
//        }
//
//        if (size > perPage) {
//            TranslatableComponent component = new TranslatableComponent("gui.strange.journal.page", lastPage);
//            GuiHelper.drawCenteredString(poseStack, font, component, midX, paginationY + 6, secondaryColor);
//
//            // only render pagination buttons on the first render pass
//            if (shouldRenderButtons) {
//                if (lastPage * perPage < size) {
//                    addRenderableWidget(new ImageButton(midX + 30, paginationY, 20, 18, 120, 0, 18, JournalScreen.NAVIGATION, b -> {
//                        ++lastPage;
//                        redraw();
//                    }));
//                }
//                if (lastPage > 1) {
//                    addRenderableWidget(new ImageButton(midX - 50, paginationY, 20, 18, 140, 0, 18, JournalScreen.NAVIGATION, b -> {
//                        --lastPage;
//                        redraw();
//                    }));
//                }
//            }
//        }
//
//        if (size == 0) {
//            GuiHelper.drawCenteredString(poseStack, font, labelForNoItems.get(), midX, 100, secondaryColor);
//        }
//    }

    protected String getTruncatedName(String name, int length) {
        return name.substring(0, Math.min(name.length(), length));
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

    protected void renderRunesString(PoseStack poseStack, String runes, int left, int top, int xOffset, int yOffset, int xMax, int yMax, boolean withShadow) {
        if (minecraft == null) return;

        // Convert the input string according to the runes that the player knows.
        String revealed = RuneHelper.revealRunes(runes, Journal2Helper.getLearnedRunes());

        int index = 0;

        for (int y = 0; y < yMax; y++) {
            for (int x = 0; x < xMax; x++) {
                if (index < revealed.length()) {
                    Component rune;
                    int color;

                    String s = String.valueOf(revealed.charAt(index));
                    if (s.equals(KnowledgeHelper.UNKNOWN)) {
                        rune = new TextComponent(KnowledgeHelper.UNKNOWN);
                        color = unknownColor;
                    } else {
                        rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
                        color = knownColor;
                    }

                    int xo = left + (x * xOffset);
                    int yo = top + (y * yOffset);

                    if (withShadow) {
                        minecraft.font.drawShadow(poseStack, rune, xo, yo, color);
                    } else {
                        minecraft.font.draw(poseStack, rune, xo, yo, color);
                    }
                }
                index++;
            }
        }
    }

    protected ItemStack getBookmarkIconItem(Bookmark bookmark) {
        var icon = bookmark.getIcon();
        return new ItemStack(Registry.ITEM.get(icon));
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

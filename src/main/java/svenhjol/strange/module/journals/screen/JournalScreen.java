package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarksScreen;
import svenhjol.strange.module.journals.screen.knowledge.*;
import svenhjol.strange.module.journals.screen.quest.JournalQuestsScreen;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.quests.QuestData;
import svenhjol.strange.module.quests.QuestsClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
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

    protected int lastPage;
    protected JournalData journal;
    protected QuestData quests;

    protected List<GuiHelper.ButtonDefinition> bottomButtons = new ArrayList<>();
    protected List<GuiHelper.ImageButtonDefinition> leftNavButtons = new ArrayList<>(); // for main journal sections
    protected List<GuiHelper.ImageButtonDefinition> bottomNavButtons = new ArrayList<>(); // dynamic for delete etc
    protected List<GuiHelper.ImageButtonDefinition> rightNavButtons = new ArrayList<>(); // dynamic for page options

    public JournalScreen(Component component) {
        super(component);

        this.passEvents = false;
        this.hasRenderedBottomButtons = false;
        this.hasRenderedNavigation = false;
        this.lastPage = 0;

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

        this.bottomButtons.add(new GuiHelper.ButtonDefinition(b -> onClose(), CLOSE));

        this.leftNavButtons.addAll(Arrays.asList(
            new GuiHelper.ImageButtonDefinition(b -> home(), NAVIGATION, 0, 36, 18, HOME_TOOLTIP),
            new GuiHelper.ImageButtonDefinition(b -> bookmarks(), NAVIGATION, 60, 36, 18, BOOKMARKS_TOOLTIP),
            new GuiHelper.ImageButtonDefinition(b -> quests(), NAVIGATION, 20, 36, 18, QUESTS_TOOLTIP),
            new GuiHelper.ImageButtonDefinition(b -> knowledge(), NAVIGATION, 40, 36, 18, KNOWLEDGE_TOOLTIP)
        ));

        JournalsClient.getJournalData().ifPresent(j -> this.journal = j);

        // ask server to update knowledge on the client
        KnowledgeClient.sendSyncKnowledge();

        // ask server to update quests on the client
        QuestsClient.sendSyncQuests();
    }

    @Override
    protected void init() {
        super.init();
        hasRenderedButtons = false;
        hasRenderedNavigation = false;
        hasRenderedBottomButtons = false;
        midX = width / 2;
        midY = width / 2;
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
            int mid = (width - backgroundWidth) / 2;
            blit(poseStack, mid, 2, 0, 0, backgroundWidth, backgroundHeight);
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
        Component title = getTitle();
        GuiHelper.drawCenteredString(poseStack, font, title, (width / 2) + titleX, titleY, titleColor);
    }

    public void renderTitleIcon(ItemStack icon) {
        // render icon next to title
        int iconX = midX - 21 - ((this.title.getString().length() * 6) / 2);
        itemRenderer.renderGuiItem(icon, iconX, titleY - 5);
    }

    public void renderNavigation(PoseStack poseStack) {
        int x;
        int y;
        int buttonWidth = 20;
        int buttonHeight = 18;

        if (!hasRenderedNavigation) {
            // render left buttons
            x = midX + leftNavX;
            y = leftNavY;
            GuiHelper.renderImageButtons(this, width, font, leftNavButtons, x, y, 0, leftNavYOffset, buttonWidth, buttonHeight);

            // render bottom buttons
            x = midX + bottomNavX;
            y = bottomNavY;
            GuiHelper.renderImageButtons(this, width, font, bottomNavButtons, x, y, 0, bottomNavYOffset, buttonWidth, buttonHeight);

            // render right buttons
            x = midX + rightNavX;
            y = rightNavY;
            GuiHelper.renderImageButtons(this, width, font, rightNavButtons, x, y, 0, rightNavYOffset, buttonWidth, buttonHeight);

            hasRenderedNavigation = true;
        }
    }

    public void renderBottomButtons(PoseStack poseStack) {
        if (!hasRenderedBottomButtons) {
            int buttonWidth = 100;
            int buttonHeight = 20;
            int xOffset = 102;

            int numberOfButtons = bottomButtons.size();

            int x = midX - ((numberOfButtons * xOffset) / 2);
            int y = (height / 4) + bottomButtonsY;

            GuiHelper.renderButtons(this, width, font, bottomButtons, x, y, xOffset, 0, buttonWidth, buttonHeight);
            hasRenderedBottomButtons = true;
        }
    }

    public void setLastPageOffset(int offset) {
        this.lastPage = offset;
    }

    protected <T> void paginator(PoseStack poseStack, List<T> items, Consumer<T> renderItem, Supplier<Component> labelForNoItems, boolean shouldRenderButtons) {
        int perPage = 6;
        int paginationY = 180;
        int currentPage = lastPage - 1;
        List<T> sublist;

        int size = items.size();
        if (size > perPage) {
            if (currentPage * perPage >= size || currentPage * perPage < 0) {
                // out of range, reset
                lastPage = 1;
                currentPage = 0;
            }
            sublist = items.subList(currentPage * perPage, Math.min(currentPage * perPage + perPage, size));
        } else {
            sublist = items;
        }

        for (T item : sublist) {
            renderItem.accept(item);
        }

        if (size > perPage) {
            TranslatableComponent component = new TranslatableComponent("gui.strange.journal.page", lastPage);
            GuiHelper.drawCenteredString(poseStack, font, component, midX, paginationY + 6, secondaryColor);

            // only render pagination buttons on the first render pass
            if (shouldRenderButtons) {
                if (lastPage * perPage < size) {
                    addRenderableWidget(new ImageButton(midX + 30, paginationY, 20, 18, 120, 0, 18, JournalScreen.NAVIGATION, b -> {
                        ++lastPage;
                        redraw();
                    }));
                }
                if (lastPage > 1) {
                    addRenderableWidget(new ImageButton(midX - 50, paginationY, 20, 18, 140, 0, 18, JournalScreen.NAVIGATION, b -> {
                        --lastPage;
                        redraw();
                    }));
                }
            }
        }

        if (size == 0) {
            GuiHelper.drawCenteredString(poseStack, font, labelForNoItems.get(), midX, 100, secondaryColor);
        }
    }

    protected void redraw() {
        ClientHelper.getClient().ifPresent(mc -> init(mc, width, height));
    }

    protected String getTruncatedName(String name, int length) {
        return name.substring(0, Math.min(name.length(), length));
    }

    protected void home() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalHomeScreen()));
    }

    protected void knowledge() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalKnowledgeScreen()));
    }

    protected void biomes() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalBiomesScreen()));
    }

    protected void runes() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalRunesScreen()));
    }

    protected void bookmarks() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalBookmarksScreen()));
    }

    protected void structures() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalStructuresScreen()));
    }

    protected void dimensions() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalDimensionsScreen()));
    }

    protected void quests() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalQuestsScreen()));
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

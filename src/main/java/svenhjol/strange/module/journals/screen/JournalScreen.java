package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.knowledge.KnowledgeClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public abstract class JournalScreen extends Screen {
    public static final ResourceLocation COVER_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_cover.png");
    public static final ResourceLocation OPEN_BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_open.png");
    public static final ResourceLocation NAVIGATION = new ResourceLocation(Strange.MOD_ID, "textures/gui/journal_navigation.png");

    public static final Component ADD_LOCATION = new TranslatableComponent("gui.strange.journal.add_location");
    public static final Component CHOOSE_ICON = new TranslatableComponent("gui.strange.journal.choose_icon");
    public static final Component CLOSE = new TranslatableComponent("gui.strange.journal.close");
    public static final Component GO_BACK = new TranslatableComponent("gui.strange.journal.go_back");
    public static final Component HOME_TOOLTIP = new TranslatableComponent("gui.strange.journal.home_tooltip");
    public static final Component JOURNAL = new TranslatableComponent("gui.strange.journal.title");
    public static final Component KNOWLEDGE = new TranslatableComponent("gui.strange.journal.knowledge");
    public static final Component KNOWLEDGE_TOOLTIP = new TranslatableComponent("gui.strange.journal.knowledge_tooltip");
    public static final Component LEARNED_BIOMES = new TranslatableComponent("gui.strange.journal.learned_biomes");
    public static final Component LEARNED_DIMENSIONS = new TranslatableComponent("gui.strange.journal.learned_dimensions");
    public static final Component LEARNED_RUNES = new TranslatableComponent("gui.strange.journal.learned_runes");
    public static final Component LEARNED_STRUCTURES = new TranslatableComponent("gui.strange.journal.learned_structures");
    public static final Component LOCATIONS = new TranslatableComponent("gui.strange.journal.locations");
    public static final Component LOCATIONS_TOOLTIP = new TranslatableComponent("gui.strange.journal.locations_tooltip");
    public static final Component MAKE_MAP = new TranslatableComponent("gui.strange.journal.make_map");
    public static final Component NO_BIOMES = new TranslatableComponent("gui.strange.journal.no_learned_biomes");
    public static final Component NO_DIMENSIONS = new TranslatableComponent("gui.strange.journal.no_learned_dimensions");
    public static final Component NO_LOCATIONS = new TranslatableComponent("gui.strange.journal.no_locations");
    public static final Component NO_STRUCTURES = new TranslatableComponent("gui.strange.journal.no_learned_structures");
    public static final Component QUESTS = new TranslatableComponent("gui.strange.journal.quests");
    public static final Component QUESTS_TOOLTIP = new TranslatableComponent("gui.strange.journal.quests_tooltip");
    public static final Component SAVE = new TranslatableComponent("gui.strange.journal.save");
    public static final Component TAKE_PHOTO = new TranslatableComponent("gui.strange.journal.take_photo");

    protected boolean hasRenderedBottomButtons;
    protected boolean hasRenderedNavigation;

    protected int backgroundWidth;
    protected int backgroundHeight;

    protected int navigationX; // navigation left relative to center
    protected int navigationY;
    protected int navigationYOffset;

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
    protected int knownColor = 0x707070;
    protected int unknownColor = 0xd0c0c0;

    protected int lastPage;
    protected JournalData journal;

    protected List<GuiHelper.ButtonDefinition> bottomButtons = new ArrayList<>();
    protected List<GuiHelper.ImageButtonDefinition> navigationButtons = new ArrayList<>();

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

        this.navigationX = -141;
        this.navigationY = 18;
        this.navigationYOffset = 18;

        this.page1Center = -56;
        this.page2Center = 56;
        this.bottomButtonsY = 158;

        this.textColor = 0x000000;
        this.secondaryColor = 0x908080;
        this.titleColor = 0x000000;
        this.errorColor = 0x770000;

        this.bottomButtons.add(new GuiHelper.ButtonDefinition(b -> onClose(), CLOSE));

        this.navigationButtons.addAll(Arrays.asList(
            new GuiHelper.ImageButtonDefinition(b -> home(), NAVIGATION, 0, 36, 18, HOME_TOOLTIP),
            new GuiHelper.ImageButtonDefinition(b -> locations(), NAVIGATION, 60, 36, 18, LOCATIONS_TOOLTIP),
            new GuiHelper.ImageButtonDefinition(b -> quests(), NAVIGATION, 20, 36, 18, QUESTS_TOOLTIP),
            new GuiHelper.ImageButtonDefinition(b -> knowledge(), NAVIGATION, 40, 36, 18, KNOWLEDGE_TOOLTIP)
        ));

        JournalsClient.getJournalData().ifPresent(j -> this.journal = j);

        // ask server to update knowledge on the client
        KnowledgeClient.sendSyncKnowledge();
    }

    @Override
    protected void init() {
        super.init();
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
        ((BaseComponent)title).setStyle(Style.EMPTY.withBold(true));
        GuiHelper.drawCenteredString(poseStack, font, title, (width / 2) + titleX, titleY, titleColor);
    }

    public void renderTitleIcon(ItemStack icon) {
        // render icon next to title
        int iconX = midX - 21 - ((this.title.getString().length() * 6) / 2);
        itemRenderer.renderGuiItem(icon, iconX, titleY - 5);
    }

    public void renderNavigation(PoseStack poseStack) {
        int x = midX + navigationX;
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

            int x = midX - ((numberOfButtons * xOffset) / 2);
            int y = (height / 4) + bottomButtonsY;

            GuiHelper.renderButtons(this, width, font, bottomButtons, x, y, xOffset, 0, buttonWidth, buttonHeight);
            hasRenderedBottomButtons = true;
        }
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

    protected void locations() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalLocationsScreen()));
    }

    protected void quests() {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalQuestsScreen()));
    }

    protected void centeredText(PoseStack poseStack, Font renderer, Component component, int x, int y, int color) {
        String string = component.getString();
        renderer.draw(poseStack, string, x - (float)(renderer.width(string) / 2), y, color);
    }
}

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

    protected int midX;
    protected int midY;

    protected int textColor = 0x000000;
    protected int secondaryColor = 0x908080;
    protected int titleColor = 0x000000;
    protected int errorColor = 0x770000;

    protected int lastPage;
    protected JournalData journal;

    protected List<GuiHelper.ButtonDefinition> bottomButtons = new ArrayList<>();
    protected List<GuiHelper.ImageButtonDefinition> navigationButtons = new ArrayList<>();

    public BaseJournalScreen(Component component) {
        super(component);
        this.passEvents = false;

        JournalsClient.getJournalData().ifPresent(j -> this.journal = j);

        // ask server to update knowledge on the client
        KnowledgeClient.sendSyncKnowledge();

        this.bottomButtons.add(
            new GuiHelper.ButtonDefinition(b -> onClose(),
                new TranslatableComponent("gui.strange.journal.close"))
        );

        this.navigationButtons.addAll(Arrays.asList(
            new GuiHelper.ImageButtonDefinition(b -> home(), NAVIGATION, 0, 36, 18,
                new TranslatableComponent("gui.strange.journal.home_tooltip")),
            new GuiHelper.ImageButtonDefinition(b -> locations(), NAVIGATION, 60, 36, 18,
                new TranslatableComponent("gui.strange.journal.locations_tooltip")),
            new GuiHelper.ImageButtonDefinition(b -> quests(), NAVIGATION, 20, 36, 18,
                new TranslatableComponent("gui.strange.journal.quests_tooltip")),
            new GuiHelper.ImageButtonDefinition(b -> knowledge(), NAVIGATION, 40, 36, 18,
                new TranslatableComponent("gui.strange.journal.knowledge_tooltip"))
        ));

        this.lastPage = 0;
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
        Component title = getTitle();
        ((BaseComponent)title).setStyle(Style.EMPTY.withBold(true));
        GuiHelper.drawCenteredString(poseStack, font, title, (width / 2) + titleX, titleY, titleColor);
    }

    public void renderTitleIcon(ItemStack icon) {
        // render icon next to title
        int iconX = width / 2 - 20 - ((this.title.getString().length() * 6) / 2);
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
                    addRenderableWidget(new ImageButton(midX + 30, paginationY, 20, 18, 120, 0, 18, BaseJournalScreen.NAVIGATION, b -> {
                        ++lastPage;
                        redraw();
                    }));
                }
                if (lastPage > 1) {
                    addRenderableWidget(new ImageButton(midX - 50, paginationY, 20, 18, 140, 0, 18, BaseJournalScreen.NAVIGATION, b -> {
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

    private String getTruncatedName(String name) {
        return getTruncatedName(name, 14);
    }


    protected void home() {
        ClientHelper.getClient().ifPresent(client
            -> client.setScreen(new JournalHomeScreen()));
    }

    protected void knowledge() {
        ClientHelper.getClient().ifPresent(client
            -> client.setScreen(new JournalKnowledgeScreen()));
    }

    protected void biomes() {
        ClientHelper.getClient().ifPresent(client
            -> client.setScreen(new JournalBiomesScreen()));
    }

    protected void runes() {
        ClientHelper.getClient().ifPresent(client
            -> client.setScreen(new JournalRunesScreen()));
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

package svenhjol.strange.module.travel_journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import svenhjol.strange.Strange;
import svenhjol.strange.module.travel_journals.TravelJournalsClient;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public abstract class TravelJournalBaseScreen extends Screen {
    protected ItemStack stack;
    protected Minecraft mc;
    protected ItemRenderer items;

    public static final int TEXT_COLOR = 0x000000;
    public static final int WARN_COLOR = 0xBB2200;
    public static final int SUB_COLOR = 0xB4B0A8;
    public static final int BGWIDTH = 256;
    public static final int BGHEIGHT = 192;
    public static final int NAME_CUTOFF = 27;

    public static Page previousPage;

    protected int leftButtonXOffset = -126;
    protected int leftButtonYOffset = 18;
    protected int rightButtonXOffset = 112;
    protected int rightButtonYOffset = 18;

    protected final int titleTop = 15;
    protected final int textRowHeight = 11;

    protected boolean hasRenderedHomeButton = false;
    protected boolean hasRenderedEntriesButton = false;
    protected boolean hasRenderedRuneButton = false;
    protected boolean hasRenderedScrollButton = false;

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/travel_journal_background.png");
    public static final ResourceLocation BUTTONS = new ResourceLocation(Strange.MOD_ID, "textures/gui/travel_journal_buttons.png");
    public static final ResourceLocation COLORS = new ResourceLocation(Strange.MOD_ID, "textures/gui/travel_journal_colors.png");

    public TravelJournalBaseScreen(String title) {
        super(new TextComponent(title));

        getClient().ifPresent(client -> this.items = client.getItemRenderer());

        this.passEvents = true;
    }

    @Override
    protected void init() {
        super.init();

        hasRenderedHomeButton = false;
        hasRenderedEntriesButton = false;
        hasRenderedRuneButton = false;
        hasRenderedScrollButton = false;

        redraw();
        refreshData();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderTravelJournalBackground(matrices, mouseX, mouseY, delta);
        this.renderTravelJournalNavigation(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        getClient().ifPresent(client -> client.setScreen(null));
    }

    public void onClose(Consumer<Minecraft> callback) {
        Optional<Minecraft> client = getClient();

        if (client.isPresent()) {
            this.onClose();
            callback.accept(client.get());
        }
    }

    public Optional<Minecraft> getClient() {
        if (this.minecraft == null)
            return Optional.empty();

        return Optional.of(this.minecraft);
    }

    protected void renderTravelJournalBackground(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        Optional<Minecraft> client = getClient();
        if (client.isPresent()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, BACKGROUND);
            int mid = (this.width - BGWIDTH) / 2;
            this.blit(matrices, mid, 2, 0, 0, BGWIDTH, BGHEIGHT);
        }
    }

    protected void renderTravelJournalNavigation(PoseStack matrices, int mouseX, int mouseY, float delta) {
        int mid = this.width / 2;
        int top = rightButtonYOffset;

        // button to open home page
        if (!hasRenderedHomeButton) {
            this.addRenderableWidget(new ImageButton(mid + rightButtonXOffset, top, 20, 18, 0, 37, 19, BUTTONS, button -> openTravelJournalScreen()));
            hasRenderedHomeButton = true;
            top += rightButtonYOffset;
        }

        // button to open entries page
        if (!hasRenderedEntriesButton) {
            this.addRenderableWidget(new ImageButton(mid + rightButtonXOffset, top, 20, 18, 60, 37, 19, BUTTONS, button -> openEntriesScreen()));
            hasRenderedEntriesButton = true;
            top += rightButtonYOffset;
        }

        // button to open rune page
        if (!hasRenderedRuneButton) {
            this.addRenderableWidget(new ImageButton(mid + rightButtonXOffset, top, 20, 18, 40, 37, 19, BUTTONS, button -> openRunesScreen()));
            hasRenderedRuneButton = true;
            top += rightButtonYOffset;
        }

        // button to open scroll page
        if (!hasRenderedScrollButton) {
            this.addRenderableWidget(new ImageButton(mid + rightButtonXOffset, top, 20, 18, 20, 37, 19, BUTTONS, button -> openScrollsScreen()));
            hasRenderedScrollButton = true;
            top += rightButtonYOffset;
        }
    }

    protected void redraw() {
        this.clearWidgets();
        this.renderButtons();
    }

    protected void centeredString(PoseStack matrices, Font textRenderer, String string, int x, int y, int color) {
        textRenderer.draw(matrices, string, x - (float)(textRenderer.width(string) / 2), y, color);
    }

    public static void centeredText(PoseStack matrices, Font textRenderer, Component text, int x, int y, int color) {
        FormattedCharSequence orderedText = text.getVisualOrderText();
        textRenderer.draw(matrices, orderedText, (float)(x - textRenderer.width(orderedText) / 2), (float)y, color);
    }

    /**
     * Allow clients to specify all buttons to render on page redraw.
     */
    protected void renderButtons() {
        // no op
    }

    /**
     * Allow clients to fetch all data when the page is first loaded.
     */
    protected void refreshData() {
        // no op
    }

    protected void openTravelJournalScreen() {
        previousPage = Page.HOME;
        TravelJournalsClient.triggerOpenTravelJournal();
    }

    protected void openEntriesScreen() {
        getClient().ifPresent(client
            -> client.setScreen(new TravelJournalEntriesScreen()));
    }

    protected void openRunesScreen() {
        getClient().ifPresent(client
            -> client.setScreen(new TravelJournalRunesScreen()));
    }

    protected void openScrollsScreen() {
        getClient().ifPresent(client
            -> client.setScreen(new TravelJournalScrollsScreen()));
    }

    public enum Page {
        HOME,
        ENTRIES,
        SCROLLS,
        RUNES,
        SKILLS,
        HELP
    }
}

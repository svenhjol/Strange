package svenhjol.strange.traveljournals.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import svenhjol.strange.Strange;
import svenhjol.strange.traveljournals.TravelJournalsClient;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class TravelJournalBaseScreen extends Screen {
    protected ItemStack stack;
    protected MinecraftClient mc;
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

    public static final Identifier BACKGROUND = new Identifier(Strange.MOD_ID, "textures/gui/travel_journal_background.png");
    public static final Identifier BUTTONS = new Identifier(Strange.MOD_ID, "textures/gui/travel_journal_buttons.png");
    public static final Identifier COLORS = new Identifier(Strange.MOD_ID, "textures/gui/travel_journal_colors.png");

    public TravelJournalBaseScreen(String title) {
        super(new LiteralText(title));

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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderTravelJournalBackground(matrices, mouseX, mouseY, delta);
        this.renderTravelJournalNavigation(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        getClient().ifPresent(client -> client.openScreen(null));
    }

    public void onClose(Consumer<MinecraftClient> callback) {
        Optional<MinecraftClient> client = getClient();

        if (client.isPresent()) {
            this.onClose();
            callback.accept(client.get());
        }
    }

    public Optional<MinecraftClient> getClient() {
        if (this.client == null)
            return Optional.empty();

        return Optional.of(this.client);
    }

    protected void renderTravelJournalBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        Optional<MinecraftClient> client = getClient();
        if (client.isPresent()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, BACKGROUND);
            int mid = (this.width - BGWIDTH) / 2;
            this.drawTexture(matrices, mid, 2, 0, 0, BGWIDTH, BGHEIGHT);
        }
    }

    protected void renderTravelJournalNavigation(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int mid = this.width / 2;
        int top = rightButtonYOffset;

        // button to open home page
        if (!hasRenderedHomeButton) {
            this.addButton(new TexturedButtonWidget(mid + rightButtonXOffset, top, 20, 18, 0, 37, 19, BUTTONS, button -> openTravelJournalScreen()));
            hasRenderedHomeButton = true;
            top += rightButtonYOffset;
        }

        // button to open entries page
        if (!hasRenderedEntriesButton) {
            this.addButton(new TexturedButtonWidget(mid + rightButtonXOffset, top, 20, 18, 60, 37, 19, BUTTONS, button -> openEntriesScreen()));
            hasRenderedEntriesButton = true;
            top += rightButtonYOffset;
        }

        // button to open rune page
        if (!hasRenderedRuneButton) {
            this.addButton(new TexturedButtonWidget(mid + rightButtonXOffset, top, 20, 18, 40, 37, 19, BUTTONS, button -> openRunesScreen()));
            hasRenderedRuneButton = true;
            top += rightButtonYOffset;
        }

        // button to open scroll page
        if (!hasRenderedScrollButton) {
            this.addButton(new TexturedButtonWidget(mid + rightButtonXOffset, top, 20, 18, 20, 37, 19, BUTTONS, button -> openScrollsScreen()));
            hasRenderedScrollButton = true;
            top += rightButtonYOffset;
        }
    }

    protected void redraw() {
        this.buttons.clear();
        this.children.clear();
        this.renderButtons();
    }

    protected void centeredString(MatrixStack matrices, TextRenderer textRenderer, String string, int x, int y, int color) {
        textRenderer.draw(matrices, string, x - (float)(textRenderer.getWidth(string) / 2), y, color);
    }

    public static void centeredText(MatrixStack matrices, TextRenderer textRenderer, Text text, int x, int y, int color) {
        OrderedText orderedText = text.asOrderedText();
        textRenderer.draw(matrices, orderedText, (float)(x - textRenderer.getWidth(orderedText) / 2), (float)y, color);
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
            -> client.openScreen(new EntriesScreen()));
    }

    protected void openRunesScreen() {
        getClient().ifPresent(client
            -> client.openScreen(new RunesScreen()));
    }

    protected void openScrollsScreen() {
        getClient().ifPresent(client
            -> client.openScreen(new ScrollsScreen()));
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

package svenhjol.strange.traveljournals.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import svenhjol.strange.Strange;

import java.util.function.Consumer;

public abstract class BaseScreen extends Screen {
    protected ItemStack stack;
    protected MinecraftClient mc;
    protected ItemRenderer items;

    public static final int TEXT_COLOR = 0x000000;
    public static final int WARN_COLOR = 0xBB2200;
    public static final int SUB_COLOR = 0xB4B0A8;
    public static final int BGWIDTH = 256;
    public static final int BGHEIGHT = 192;
    public static final int NAME_CUTOFF = 27;

    protected int leftButtonXOffset = -127;
    protected int leftButtonYOffset = 18;
    protected int rightButtonXOffset = 113;
    protected int rightButtonYOffset = 18;

    public static final Identifier BACKGROUND = new Identifier(Strange.MOD_ID, "textures/gui/travel_journal_background.png");
    public static final Identifier BUTTONS = new Identifier(Strange.MOD_ID, "textures/gui/travel_journal_buttons.png");
    public static final Identifier COLORS = new Identifier(Strange.MOD_ID, "textures/gui/travel_journal_colors.png");

    public BaseScreen(String title) {
        super(new LiteralText(title));

        if (this.client != null)
            this.items = this.client.getItemRenderer();

        this.passEvents = true;
    }

    @Override
    protected void init() {
        super.init();
        redraw();
        refreshData();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderTravelJournalBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (this.client != null)
            this.client.openScreen(null);
    }

    public void onClose(Consumer<MinecraftClient> callback) {
        if (this.client != null) {
            this.onClose();
            callback.accept(this.client);
        }
    }

    protected void renderTravelJournalBackground(MatrixStack matrices) {
        this.renderBackground(matrices);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.client != null) {
            this.client.getTextureManager().bindTexture(BACKGROUND);
            int mid = (this.width - BGWIDTH) / 2;
            this.drawTexture(matrices, mid, 2, 0, 0, BGWIDTH, BGHEIGHT);
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

    protected void renderButtons() {
        // no op
    }

    protected void refreshData() {
        // no op
    }

    protected boolean isClientValid() {
        return client != null && client.world != null && client.player != null;
    }
}

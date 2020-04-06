package svenhjol.strange.traveljournal.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import svenhjol.strange.Strange;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class BaseTravelJournalScreen extends Screen implements IRenderable {
    protected PlayerEntity player;
    protected Hand hand;
    protected ItemStack stack;
    protected Minecraft mc;
    protected ItemRenderer items;

    public static final int TEXT_COLOR = 0x000000;
    public static final int WARN_COLOR = 0xBB2200;
    public static final int SUB_COLOR = 0xB4B0A8;
    public static final int BGWIDTH = 256;
    public static final int BGHEIGHT = 192;
    public static final int UPDATE_TIMEOUT = 3;
    public static final ResourceLocation BACKGROUND = new ResourceLocation(Strange.MOD_ID, "textures/gui/travel_journal.png");
    public static final ResourceLocation BUTTONS = new ResourceLocation(Strange.MOD_ID, "textures/gui/gui_buttons.png");
    public static final ResourceLocation COLORS = new ResourceLocation(Strange.MOD_ID, "textures/gui/gui_colors.png");

    public BaseTravelJournalScreen(String title, PlayerEntity player, Hand hand) {
        super(new StringTextComponent(title));
        this.player = player;
        this.hand = hand;
        this.stack = player.getHeldItem(hand);
        this.mc = Minecraft.getInstance();
        this.items = mc.getItemRenderer();
        this.passEvents = true;
    }

    @Override
    protected void init() {
        redraw();
        refreshData();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.close();
    }

    protected void close() {
        if (this.minecraft != null) {
            this.minecraft.displayGuiScreen(null);
        }
    }

    protected void close(Consumer<Minecraft> onClose) {
        if (this.minecraft != null) {
            this.minecraft.displayGuiScreen(null);
            onClose.accept(this.minecraft);
        }
    }

    protected void redraw() {
        this.buttons.clear();
        this.children.clear();
        this.renderButtons();
    }

    protected void renderButtons() {
        // no op
    }

    protected void renderBackgroundTexture() {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.minecraft != null) {
            this.minecraft.getTextureManager().bindTexture(BACKGROUND);
            int w = (this.width - BGWIDTH) / 2;
            this.blit(w, 2, 0, 0, BGWIDTH, BGHEIGHT);
        }
    }

    protected void refreshData() {
        // no op
    }

    @Override
    public void drawCenteredString(FontRenderer renderer, String str, int x, int y, int color) {
        renderer.drawString(str, (float) (x - renderer.getStringWidth(str) / 2), (float) y, color);
    }

    @Override
    public void drawRightAlignedString(FontRenderer renderer, String str, int x, int y, int color) {
        renderer.drawString(str, (float) (x - renderer.getStringWidth(str)), (float) y, color);
    }

    public void blitItemIcon(ItemStack stack, int x, int y) {
        RenderHelper.enableGUIStandardItemLighting();
        items.renderItemIntoGUI(stack, x, y);
    }
}

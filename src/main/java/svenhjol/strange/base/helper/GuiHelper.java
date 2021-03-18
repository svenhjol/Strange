package svenhjol.strange.base.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import svenhjol.strange.base.StrangeIcons;

// TODO: move to Charm when complete
public class GuiHelper extends DrawableHelper {
    public static ItemRenderer getItemRenderer() {
        return MinecraftClient.getInstance().getItemRenderer();
    }

    public static TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static TextureManager getTextureManager() {
        return MinecraftClient.getInstance().getTextureManager();
    }

    public static void drawCenteredTitle(MatrixStack matrices, Text title, int left, int top, int color) {
        DrawableHelper.drawCenteredText(matrices, getTextRenderer(), title, left, top, color);
    }

    public static void renderItemStack(ItemStack stack, int x, int y) {
        getItemRenderer().renderGuiItemIcon(stack, x, y);
    }

    public void renderIcon(MatrixStack matrices, int[] icon, int x, int y) {
        int w = StrangeIcons.ICON_WIDTH;
        int h = StrangeIcons.ICON_HEIGHT;

        RenderSystem.setShaderTexture(0, StrangeIcons.ICONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexture(matrices, x, y, 256 - (icon[0] * w), icon[1] * h, w, h);
    }

    public static void renderIcon(Screen screen, MatrixStack matrices, int[] icon, int x, int y) {
        int w = StrangeIcons.ICON_WIDTH;
        int h = StrangeIcons.ICON_HEIGHT;

        RenderSystem.setShaderTexture(0, StrangeIcons.ICONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        screen.drawTexture(matrices, x, y, 256 - (icon[0] * w), icon[1] * h, w, h);
    }
}

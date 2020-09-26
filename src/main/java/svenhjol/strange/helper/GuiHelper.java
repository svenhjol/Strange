package svenhjol.strange.helper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class GuiHelper extends DrawableHelper {
    public static ItemRenderer getItemRenderer() {
        return MinecraftClient.getInstance().getItemRenderer();
    }

    public static TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static void drawCenteredTitle(MatrixStack matrices, String title, int left, int top, int color) {
        DrawableHelper.drawCenteredString(matrices, getTextRenderer(), title, left, top, color);
    }

    public static void renderItemIcon(ItemStack stack, int x, int y) {
        DiffuseLighting.enable();
        getItemRenderer().renderGuiItemIcon(stack, x, y);
    }
}

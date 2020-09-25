package svenhjol.strange.helper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class GuiHelper extends DrawableHelper {
    public static TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static void drawCenteredTitle(MatrixStack matrices, String title, int xpos, int ypos) {
        DrawableHelper.drawCenteredString(matrices, getTextRenderer(), title, xpos, ypos, 0xFFFFFF);
    }
}

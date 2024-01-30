package svenhjol.strange.helper;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiHelper {
    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, Component component, int x, int y, int color, boolean dropShadow) {
        var formattedCharSequence = component.getVisualOrderText();
        guiGraphics.drawString(font, formattedCharSequence, x - font.width(formattedCharSequence) / 2, y, color, dropShadow);
    }
}

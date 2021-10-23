package svenhjol.strange.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;

public class GuiHelper {
    public static void renderButtons(Screen screen, int screenWidth, Font font, List<ButtonDefinition> buttons, int x, int y, int xOffset, int yOffset, int buttonWidth, int buttonHeight) {
        for (GuiHelper.ButtonDefinition b : buttons) {
            Button.OnTooltip tooltip = b.tooltip != null ? (button, p, tx, ty) -> screen.renderTooltip(p, font.split(b.tooltip, Math.max(screenWidth / 2 - 43, 170)), tx, ty) : (button, p, tx, ty) -> {};
            screen.addRenderableWidget(new Button(x, y, buttonWidth, buttonHeight, b.name, b.action, tooltip));

            x += xOffset;
            y += yOffset;
        }
    }

    public static void renderImageButtons(Screen screen, int screenWidth, Font font, List<ImageButtonDefinition> buttons, int x, int y, int xOffset, int yOffset, int buttonWidth, int buttonHeight) {
        for (GuiHelper.ImageButtonDefinition b : buttons) {
            Button.OnTooltip tooltip = b.tooltip != null ? (button, p, tx, ty) -> screen.renderTooltip(p, font.split(b.tooltip, Math.max(screenWidth / 2 - 43, 170)), tx, ty) : (button, p, tx, ty) -> {};
            screen.addRenderableWidget(new ImageButton(x, y, buttonWidth, buttonHeight, b.texX, b.texY, b.texHoverOffset, b.texture, 256, 256, b.action, tooltip, TextComponent.EMPTY));

            x += xOffset;
            y += yOffset;
        }
    }

    /**
     * Copy of Gui#drawCenteredString that draws font without shadow effect.
     */
    public static void drawCenteredString(PoseStack poseStack, Font font, Component component, int left, int top, int color) {
        FormattedCharSequence formattedCharSequence = component.getVisualOrderText();
        font.draw(poseStack, formattedCharSequence, (float)(left - font.width(formattedCharSequence) / 2), (float)top, color);
    }

    public static class ButtonDefinition {
        public final Component name;
        public final Component tooltip;
        public final Button.OnPress action;

        public ButtonDefinition(Button.OnPress action, @Nullable Component name) {
            this(action, name, null);
        }

        public ButtonDefinition(Button.OnPress action, @Nullable Component name, @Nullable Component tooltip) {
            this.name = name;
            this.action = action;
            this.tooltip = tooltip;
        }
    }

    public static class ImageButtonDefinition {
        public final Button.OnPress action;
        public final Component tooltip;
        public final ResourceLocation texture;
        public final int texX;
        public final int texY;
        public final int texHoverOffset;

        public ImageButtonDefinition(Button.OnPress action, ResourceLocation texture, int texX, int texY, int texHoverOffset) {
            this(action, texture, texX, texY, texHoverOffset, null);
        }

        public ImageButtonDefinition(Button.OnPress action, ResourceLocation texture, int texX, int texY, int texHoverOffset, @Nullable Component tooltip) {
            this.action = action;
            this.tooltip = tooltip;
            this.texture = texture;
            this.texX = texX;
            this.texY = texY;
            this.texHoverOffset = texHoverOffset;
        }
    }
}

package svenhjol.strange.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class GuiHelper {
    public static void addButtons(Screen screen, int screenWidth, Font font, List<ButtonDefinition> buttons, int x, int y, int xOffset, int yOffset, int buttonWidth, int buttonHeight) {
        for (GuiHelper.ButtonDefinition b : buttons) {
            Button.OnTooltip tooltip = b.tooltip != null ? (button, p, tx, ty) -> screen.renderTooltip(p, font.split(b.tooltip, Math.max(screenWidth / 2 - 43, 170)), tx, ty) : (button, p, tx, ty) -> {};

            var button = new Button(x, y, buttonWidth, buttonHeight, b.name, b.action, tooltip);
            if (b.afterCreated != null) {
                b.afterCreated.accept(button);
            }

            screen.addRenderableWidget(button);

            x += xOffset;
            y += yOffset;
        }
    }

    public static void addImageButtons(Screen screen, int screenWidth, Font font, List<ImageButtonDefinition> buttons, int x, int y, int xOffset, int yOffset, int buttonWidth, int buttonHeight) {
        for (GuiHelper.ImageButtonDefinition b : buttons) {
            Button.OnTooltip tooltip = b.tooltip != null ? (button, p, tx, ty) -> screen.renderTooltip(p, font.split(b.tooltip, Math.max(screenWidth / 2 - 43, 170)), tx, ty) : (button, p, tx, ty) -> {};

            var button = new ImageButton(x, y, buttonWidth, buttonHeight, b.texX, b.texY, b.texHoverOffset, b.texture, 256, 256, b.action, tooltip, TextComponent.EMPTY);
            if (b.afterCreated != null) {
                b.afterCreated.accept(button);
            }

            screen.addRenderableWidget(button);

            x += xOffset;
            y += yOffset;
        }
    }

    /**
     * Copy of {@link net.minecraft.client.gui.Gui#drawCenteredString} that draws font without shadow effect.
     */
    public static void drawCenteredString(PoseStack poseStack, Font font, Component component, int left, int top, int color) {
        FormattedCharSequence formattedCharSequence = component.getVisualOrderText();
        font.draw(poseStack, formattedCharSequence, (float)(left - font.width(formattedCharSequence) / 2), (float)top, color);
    }

    /**
     * Copy of {@link Font#drawWordWrap} that returns the new vertical position after rendering multilines.
     */
    public static int drawWordWrap(Font font, FormattedText formattedText, int left, int top, int length, int color) {
        Matrix4f matrix4f = Transformation.identity().getMatrix();
        for (FormattedCharSequence formattedCharSequence : font.split(formattedText, length)) {
            font.drawInternal(formattedCharSequence, left, top, color, matrix4f, false);
            top += 9;
        }

        return top;
    }

    public static class ButtonDefinition {
        public final Component name;
        public final Component tooltip;
        public final Button.OnPress action;
        public final Consumer<AbstractButton> afterCreated;

        public ButtonDefinition(Button.OnPress action, @Nullable Component name) {
            this(action, name, null);
        }

        public ButtonDefinition(Button.OnPress action, @Nullable Component name, @Nullable Component tooltip) {
            this(action, name, tooltip, null);
        }

        public ButtonDefinition(Button.OnPress action, @Nullable Component name, @Nullable Component tooltip, @Nullable Consumer<AbstractButton> afterCreated) {
            this.name = name;
            this.action = action;
            this.tooltip = tooltip;
            this.afterCreated = afterCreated;
        }
    }

    public static class ImageButtonDefinition {
        public final Button.OnPress action;
        public final Component tooltip;
        public final ResourceLocation texture;
        public final int texX;
        public final int texY;
        public final int texHoverOffset;
        public final Consumer<AbstractButton> afterCreated;

        public ImageButtonDefinition(Button.OnPress action, ResourceLocation texture, int texX, int texY, int texHoverOffset) {
            this(action, texture, texX, texY, texHoverOffset, null);
        }

        public ImageButtonDefinition(Button.OnPress action, ResourceLocation texture, int texX, int texY, int texHoverOffset, @Nullable Component tooltip) {
            this(action, texture, texX, texY, texHoverOffset, tooltip, null);
        }

        public ImageButtonDefinition(Button.OnPress action, ResourceLocation texture, int texX, int texY, int texHoverOffset, @Nullable Component tooltip, @Nullable Consumer<AbstractButton> afterCreated) {
            this.action = action;
            this.tooltip = tooltip;
            this.texture = texture;
            this.texX = texX;
            this.texY = texY;
            this.texHoverOffset = texHoverOffset;
            this.afterCreated = afterCreated;
        }
    }
}

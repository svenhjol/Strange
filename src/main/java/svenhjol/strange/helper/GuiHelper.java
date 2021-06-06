package svenhjol.strange.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.init.StrangeIcons;

// TODO: move to Charm
public class GuiHelper extends GuiComponent {
    public static ItemRenderer getItemRenderer() {
        return Minecraft.getInstance().getItemRenderer();
    }

    public static Font getTextRenderer() {
        return Minecraft.getInstance().font;
    }

    public static TextureManager getTextureManager() {
        return Minecraft.getInstance().getTextureManager();
    }

    public static void drawCenteredTitle(PoseStack matrices, Component title, int left, int top, int color) {
        GuiComponent.drawCenteredString(matrices, getTextRenderer(), title, left, top, color);
    }

    public static void renderItemStack(ItemStack stack, int x, int y) {
        getItemRenderer().renderGuiItem(stack, x, y);
    }

    public void renderIcon(PoseStack matrices, int[] icon, int x, int y) {
        int w = StrangeIcons.ICON_WIDTH;
        int h = StrangeIcons.ICON_HEIGHT;

        RenderSystem.setShaderTexture(0, StrangeIcons.ICONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(matrices, x, y, 256 - (icon[0] * w), icon[1] * h, w, h);
    }

    public static void renderIcon(Screen screen, PoseStack matrices, int[] icon, int x, int y) {
        int w = StrangeIcons.ICON_WIDTH;
        int h = StrangeIcons.ICON_HEIGHT;

        RenderSystem.setShaderTexture(0, StrangeIcons.ICONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        screen.blit(matrices, x, y, 256 - (icon[0] * w), icon[1] * h, w, h);
    }
}

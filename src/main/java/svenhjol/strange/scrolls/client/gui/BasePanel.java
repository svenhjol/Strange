package svenhjol.strange.scrolls.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import svenhjol.strange.scrolls.client.QuestIcons;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public abstract class BasePanel extends AbstractGui
{
    protected IQuest quest;
    protected Minecraft mc;
    protected FontRenderer fonts;
    protected ItemRenderer items;
    protected TextureManager textures;
    protected int primaryTextColor = 0xFFFFFF;
    protected int secondaryTextColor = 0xAAAAAA;
    protected int pad = 8;
    protected int width;
    protected int mid;

    public BasePanel(IQuest quest, int mid, int width)
    {
        this.quest = quest;
        this.width = width;
        this.mid = mid;
        this.mc = Minecraft.getInstance();
        this.fonts = mc.fontRenderer;
        this.items = mc.getItemRenderer();
        this.textures = mc.getTextureManager();
    }

    public void drawBackground(int x0, int x1, int y0, int y1)
    {
        // draw background rect
        AbstractGui.fill(x0, y0, x1, y1, 0x88000000);
    }

    public void drawCenteredTitle(String title, int y)
    {
        this.drawCenteredString(fonts, title, mid, y, primaryTextColor);
    }

    public void blitIcon(int[] icon, int x, int y)
    {
        int w = QuestIcons.ICON_WIDTH;
        int h = QuestIcons.ICON_HEIGHT;

        textures.bindTexture(QuestIcons.ICONS);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        blit(x, y, 256 - (icon[0] * w), icon[1] * h, w, h);
    }

    public void blitItemIcon(ItemStack stack, int x, int y)
    {
        items.renderItemIntoGUI(stack, x, y);
    }
}

package svenhjol.strange.scrolls.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import svenhjol.strange.scrolls.client.QuestIcons;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public abstract class BasePanel extends AbstractGui
{
    protected IQuest quest;
    protected int width;
    protected Minecraft mc;
    protected FontRenderer fonts;
    protected ItemRenderer items;
    protected TextureManager textures;
    protected int primaryTextColor = 0xFFFFFF;
    protected int secondaryTextColor = 0xAAAAAA;
    protected int pad = 8;

    public BasePanel(IQuest quest, int width)
    {
        this.quest = quest;
        this.width = width;
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

    public void blitIcon(int x, int y, int[] icon)
    {
        int w = QuestIcons.ICON_WIDTH;
        int h = QuestIcons.ICON_HEIGHT;

        textures.bindTexture(QuestIcons.ICONS);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        blit(x, y, 256 - (icon[0] * w), icon[1] * h, w, h);
    }

    public void blitItemIcon(int x, int y, String name)
    {
        textures.bindTexture(new ResourceLocation( "textures/item/" + name + ".png"));
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        blit(x, y, 0, 0, 16, 16);
    }
}

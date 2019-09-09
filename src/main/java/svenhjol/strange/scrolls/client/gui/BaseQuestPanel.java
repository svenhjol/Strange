package svenhjol.strange.scrolls.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import svenhjol.strange.scrolls.quest.IQuest;

public abstract class BaseQuestPanel extends AbstractGui
{
    protected IQuest quest;
    protected int width;
    protected Minecraft mc;
    protected FontRenderer fonts;
    protected ItemRenderer items;
    protected TextureManager textures;
    protected int primaryTextColor = 0xFFFFFF;
    protected int secondaryTextColor = 0xAAAAAA;

    public BaseQuestPanel(IQuest quest, int width)
    {
        this.quest = quest;
        this.width = width;
        this.mc = Minecraft.getInstance();
        this.fonts = mc.fontRenderer;
        this.items = mc.getItemRenderer();
        this.textures = mc.getTextureManager();
    }

    public void renderIcon(ResourceLocation icon, int x, int y, int offset)
    {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        textures.bindTexture(icon);
        AbstractGui.blit(x, y, offset, 0.0F, 0.0F, 18, 18, 128, 128);
    }
}

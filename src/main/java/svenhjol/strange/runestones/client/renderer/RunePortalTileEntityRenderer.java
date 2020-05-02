package svenhjol.strange.runestones.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import svenhjol.strange.runestones.tileentity.RunePortalTileEntity;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.Random;

public class RunePortalTileEntityRenderer extends TileEntityRenderer<RunePortalTileEntity> {
    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation RUNE_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

    public static float colorTicks = 0.0F;

    public void render(RunePortalTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.disableLighting();

        RANDOM.setSeed(10L);
        GlStateManager.getMatrix(2982, MODELVIEW);
        GlStateManager.getMatrix(2983, PROJECTION);
        int i = 13;
        float f = this.getOffset();
        GameRenderer gamerenderer = Minecraft.getInstance().gameRenderer;

        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;

        if (tile.color == -1) {
            final int rgb = Color.HSBtoRGB(colorTicks, 1.0F, 0.5F);
            colorTicks += 0.0001F;
            if (colorTicks >= 1.0F) colorTicks = 0F;

            r = ((rgb >> 16) & 0xFF) / 255F;
            g = ((rgb >> 8) & 0xFF) / 255F;
            b = (rgb & 0xFF) / 255F;
        }

        for (int j = 0; j < i; ++j) {
            GlStateManager.pushMatrix();
            float f1 = 8.0F / (float) (30 - j);

            if (j == 0) {
                this.bindTexture(END_SKY_TEXTURE);
                f1 = 0.05F;
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            if (j >= 1) {
                this.bindTexture(RUNE_PORTAL_TEXTURE);
                gamerenderer.setupFogColor(true);
            }

            if (j == 1) {
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }

            GlStateManager.texGenMode(GlStateManager.TexGen.S, 9216);
            GlStateManager.texGenMode(GlStateManager.TexGen.T, 9216);
            GlStateManager.texGenMode(GlStateManager.TexGen.R, 9216);
            GlStateManager.texGenParam(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.texGenParam(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GlStateManager.texGenParam(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GlStateManager.enableTexGen(GlStateManager.TexGen.S);
            GlStateManager.enableTexGen(GlStateManager.TexGen.T);
            GlStateManager.enableTexGen(GlStateManager.TexGen.R);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translatef(0.25F, 0.25F, 0.0F);
            GlStateManager.scalef(0.4F, 0.4F, 1.0F);
            float f2 = (j + 6) * 0.75F;
            GlStateManager.translatef(17.0F / f2, (2.0F + f2 / 1.5F) * ((float) (Util.milliTime() % 200000L) / 200000.0F), 0.0F);
            GlStateManager.rotatef((f2 * f2 * 4321.0F + f2 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);

            GlStateManager.scalef(4.1F - f2 / 4.0F, 4.1F - f2 / 4.0F, 1.0F);

            GlStateManager.multMatrix(PROJECTION);
            GlStateManager.multMatrix(MODELVIEW);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

            int k = j - 1;

            if (tile.color >= 0) {
                DyeColor dyeColor = DyeColor.byId(tile.color);
                float[] comps = dyeColor.getColorComponentValues();
                r = comps[0];
                g = comps[1];
                b = comps[2];

                r *= 1.7F * f1;
                g *= 1.7F * f1;
                b *= 1.7F * f1;
            }

            if (tile.orientation == 0) { // x axis
                // north
                bufferbuilder.pos(x, y + 1.0D, z + 0.25D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y + 1.0D, z + 0.25D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y, z + 0.25D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x, y, z + 0.25D).color(r, g, b, 1.0F).endVertex();
                // south
                bufferbuilder.pos(x, y, z + 0.75D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y, z + 0.75D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y + 1.0D, z + 0.75D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x, y + 1.0D, z + 0.75D).color(r, g, b, 1.0F).endVertex();
            } else if (tile.orientation == 1) { // z axis
                // east
                bufferbuilder.pos(x + 0.75D, y + 1.0D, z).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 0.75D, y + 1.0D, z + 1.0D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 0.75D, y, z + 1.0D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 0.75D, y, z).color(r, g, b, 1.0F).endVertex();
                // west
                bufferbuilder.pos(x + 0.25D, y, z).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 0.25D, y, z + 1.0D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 0.25D, y + 1.0D, z + 1.0D).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(x + 0.25D, y + 1.0D, z).color(r, g, b, 1.0F).endVertex();
            }

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            this.bindTexture(END_SKY_TEXTURE);
        }

        GlStateManager.disableBlend();
        GlStateManager.disableTexGen(GlStateManager.TexGen.S);
        GlStateManager.disableTexGen(GlStateManager.TexGen.T);
        GlStateManager.disableTexGen(GlStateManager.TexGen.R);
        GlStateManager.enableLighting();
        gamerenderer.setupFogColor(false);
    }

    protected float getOffset() {
        return 0.75F;
    }

    private FloatBuffer getBuffer(float p_147525_1_, float p_147525_2_, float p_147525_3_, float p_147525_4_) {
        this.buffer.clear();
        this.buffer.put(p_147525_1_).put(p_147525_2_).put(p_147525_3_).put(p_147525_4_);
        this.buffer.flip();
        return this.buffer;
    }
}

package svenhjol.strange.traveljournal.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.traveljournal.Entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class PageItemRenderer implements AutoCloseable {
    public final RenderType RUNE_PAGE_BACKGROUND = RenderType.getText(new ResourceLocation(Strange.MOD_ID, "textures/gui/rune_page_background.png"));
    public final RenderType ENTRY_PAGE_BACKGROUND = RenderType.getText(new ResourceLocation(Strange.MOD_ID, "textures/gui/entry_page_background.png"));
    public final TextureManager textureManager;
    public final Map<String, Instance> instances = new HashMap<>();
    private FontRenderer fr;
    private FontRenderer gr;

    public PageItemRenderer(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public PageItemRenderer.Instance getInstance(Entry entry) {
        PageItemRenderer.Instance i = this.instances.get(entry.posref);
        if (i == null) {
            i = new PageItemRenderer.Instance(entry);
            this.instances.put(entry.posref, i);
        }
        return i;
    }

    public void clearInstances() {
        for (Instance i : this.instances.values()) {
            i.close();
        }
        this.instances.clear();
    }

    public void renderRunePage(Entry entry, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
        this.getInstance(entry).renderRunePage(entry, matrixStack, buffers, light);
    }

    public void renderEntryPage(Entry entry, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
        this.getInstance(entry).renderEntryPage(entry, matrixStack, buffers, light);
    }

    @Override
    public void close() {
        this.clearInstances();
    }

    public class Instance implements AutoCloseable {
        private final Entry entry;
        private String discovered;
        protected File file = null;
        protected DynamicTexture tex = null;
        protected ResourceLocation res = null;
        protected RenderType renderScreenshot = null;

        private Instance(Entry entry) {
            this.entry = entry;
            this.discovered = null;

            try {
                file = new File(new File(Minecraft.getInstance().gameDir, "screenshots"), entry.id + ".png");
                final RandomAccessFile raf = new RandomAccessFile(file, "r");
                if (raf != null)
                    raf.close();
                InputStream stream = new FileInputStream(file);
                NativeImage screenshot = NativeImage.read(stream);
                tex = new DynamicTexture(screenshot);
                res = PageItemRenderer.this.textureManager.getDynamicTextureLocation("screenshot", tex);
                renderScreenshot = RenderType.getText(res);
                stream.close();

            } catch (Exception e) {
                // ignore screenshot
            }
        }

        public void renderEntryPage(Entry entry, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
            this.renderEntryPageBackground(matrixStack, buffers, light);
            fr = Minecraft.getInstance().fontRenderer;

            if (this.renderScreenshot != null) {
                Matrix4f matrix4f = matrixStack.getLast().getMatrix();
                IVertexBuilder ivertexbuilder = buffers.getBuffer(this.renderScreenshot);
                ivertexbuilder.pos(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).tex(0.0F, 1.0F).lightmap(light).endVertex();
                ivertexbuilder.pos(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).tex(1.0F, 1.0F).lightmap(light).endVertex();
                ivertexbuilder.pos(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).tex(1.0F, 0.0F).lightmap(light).endVertex();
                ivertexbuilder.pos(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).tex(0.0F, 0.0F).lightmap(light).endVertex();
            }

            matrixStack.push();
            int color = DyeColor.byId(entry.color).getColorValue();
            int bgcolor = this.renderScreenshot == null ? 0x00000000 : 0x77FFFFFF;
            int x = 62 - (6 * (entry.name.length() / 2));
            fr.renderString(entry.name, x, 14, color, false, matrixStack.getLast().getMatrix(), buffers, false, bgcolor, light);

            if (this.renderScreenshot == null) {
                // render the runes for this entry
                discovered = RunestoneHelper.getDiscoveredRunesClient(entry);
                for (int i = 0; i < discovered.length(); i++) {
                    renderGlyph(discovered.substring(i, i + 1), 25 + (7 * i), 110, matrixStack, buffers, light, 0xFF404040, 0x9A404040);
                }
            }

            matrixStack.pop();
        }

        public void renderRunePage(Entry entry, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
            this.renderRunePageBackground(matrixStack, buffers, light);

            fr = Minecraft.getInstance().fontRenderer;
            gr = Minecraft.getInstance().getFontResourceManager().getFontRenderer(Minecraft.standardGalacticFontRenderer);
            discovered = RunestoneHelper.getDiscoveredRunesClient(entry);

            if (discovered.length() != 12)
                return;

            matrixStack.push();
            int x = 0;
            int y = 0;
            int c = 0;

            for (int i = 0; i < 3; i++) {
                renderGlyph(discovered.substring(c++, c), x + 13, y + 85 - (i * 25), matrixStack, buffers, light, 0xFFFFFFFF, 0xFF808080);
            }
            for (int i = 0; i < 3; i++) {
                renderGlyph(discovered.substring(c++, c), x + 38 + (i * 24), y + 11, matrixStack, buffers, light, 0xFFFFFFFF, 0xFF808080);
            }
            for (int i = 0; i < 3; i++) {
                renderGlyph(discovered.substring(c++, c), x + 111, y + 36 + (i * 24), matrixStack, buffers, light, 0xFFFFFFFF, 0xFF808080);
            }
            for (int i = 0; i < 3; i++) {
                renderGlyph(discovered.substring(c++, c), x + 86 - (i * 24), y + 109, matrixStack, buffers, light, 0xFFFFFFFF, 0xFF808080);
            }

            matrixStack.pop();
        }

        private void renderGlyph(String s, float x, float y, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, int runeColor, int unknownColor) {
            // 0xff23183a

            int color;

            FontRenderer f;
            if (s.equals("?")) {
                f = fr;
                color = unknownColor;
            } else {
                f = gr;
                color = runeColor;
            }
            f.renderString(s, x, y, color, false, matrixStack.getLast().getMatrix(), buffers, false, 0x00000000, light);
        }

        private void renderRunePageBackground(MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
            this.renderBackground(RUNE_PAGE_BACKGROUND, matrixStack, buffers, light);
        }

        private void renderEntryPageBackground(MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
            this.renderBackground(ENTRY_PAGE_BACKGROUND, matrixStack, buffers, light);
        }

        private void renderBackground(RenderType background, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
            matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F));
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(180.0F));
            matrixStack.scale(0.38F, 0.38F, 0.38F);
            matrixStack.translate(-0.5D, -0.5D, 0.0D);
            matrixStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
            IVertexBuilder builder = buffers.getBuffer(background);
            Matrix4f matrix4f = matrixStack.getLast().getMatrix();
            builder.pos(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).tex(0.0F, 1.0F).lightmap(light).endVertex();
            builder.pos(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).tex(1.0F, 1.0F).lightmap(light).endVertex();
            builder.pos(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).tex(1.0F, 0.0F).lightmap(light).endVertex();
            builder.pos(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).tex(0.0F, 0.0F).lightmap(light).endVertex();
        }

        @Override
        public void close() {

        }
    }


    public void renderArm(ClientPlayerEntity player, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, float swing, float equip, Hand hand) {
        // render arm
        float e = hand == Hand.MAIN_HAND ? 1.0F : -1.0F;

        // copypasta from renderArmFirstPerson
        float e1 = MathHelper.sqrt(swing);
        float e2 = -0.3F * MathHelper.sin(e1 * (float)Math.PI);
        float e3 = 0.4F * MathHelper.sin(e1 * ((float)Math.PI * 2F));
        float e4 = -0.4F * MathHelper.sin(swing * (float)Math.PI);
        matrixStack.translate((double)(e * (e2 + 0.64000005F)), (double)(e3 + -0.6F + equip * -0.6F), (double)(e4 + -0.71999997F));
        matrixStack.rotate(Vector3f.YP.rotationDegrees(e * 45.0F));
        float e5 = MathHelper.sin(swing * swing * (float)Math.PI);
        float e6 = MathHelper.sin(e1 * (float)Math.PI);
        matrixStack.rotate(Vector3f.YP.rotationDegrees(e * e6 * 70.0F));
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(e * e5 * -20.0F));
        Minecraft.getInstance().getTextureManager().bindTexture(player.getLocationSkin());
        matrixStack.translate((double)(e * -1.0F), (double)3.6F, 3.5D);
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(e * 120.0F));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(200.0F));
        matrixStack.rotate(Vector3f.YP.rotationDegrees(e * -135.0F));
        matrixStack.translate((double)(e * 5.6F), 0.0D, 0.0D);
        PlayerRenderer playerrenderer = (PlayerRenderer)Minecraft.getInstance().getRenderManager().<AbstractClientPlayerEntity>getRenderer(player);
        if (hand == Hand.MAIN_HAND) {
            playerrenderer.renderRightArm(matrixStack, buffers, light, player);
        } else {
            playerrenderer.renderLeftArm(matrixStack, buffers, light, player);
        }
    }

    public void transformPageForHand(MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, float swing, float equip, Hand hand) {
        float e = hand == Hand.MAIN_HAND ? 1.0F : -1.0F;
        matrixStack.translate((double)(e * 0.51F), (double)(-0.08F + equip * -1.2F), -0.75D);
        float f1 = MathHelper.sqrt(swing);
        float f2 = MathHelper.sin(f1 * (float)Math.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * MathHelper.sin(f1 * ((float)Math.PI * 2F));
        float f5 = -0.3F * MathHelper.sin(swing * (float)Math.PI);
        matrixStack.translate((double)(e * f3), (double)(f4 - 0.3F * f2), (double)f5);
        matrixStack.rotate(Vector3f.XP.rotationDegrees(f2 * -45.0F));
        matrixStack.rotate(Vector3f.YP.rotationDegrees(e * f2 * -30.0F));
    }
}

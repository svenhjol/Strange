package svenhjol.strange.traveljournal.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import svenhjol.strange.traveljournal.Entry;

public class RunePageRenderer implements AutoCloseable {
    public final TextureManager textureManager;
    private FontRenderer fr;
    private FontRenderer gr;

    public RunePageRenderer(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    @Override
    public void close() {
    }

    public void render(Entry entry, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
        fr = Minecraft.getInstance().fontRenderer;
        gr = Minecraft.getInstance().getFontResourceManager().getFontRenderer(Minecraft.standardGalacticFontRenderer);

        String known = entry.known;
        if (known.length() != 12)
            known = "????????????";

        matrixStack.push();
        int x = 0;
        int y = 0;
        int c = 0;

        for (int i = 0; i < 3; i++) {
            renderGlyph(known.substring(c++, c), x + 13, y + 85 - (i * 25), matrixStack, buffers, light);
        }
        for (int i = 0; i < 3; i++) {
            renderGlyph(known.substring(c++, c), x + 38 + (i * 24), y + 11, matrixStack, buffers, light);
        }
        for (int i = 0; i < 3; i++) {
            renderGlyph(known.substring(c++, c), x + 111, y + 36 + (i * 24), matrixStack, buffers, light);
        }
        for (int i = 0; i < 3; i++) {
            renderGlyph(known.substring(c++, c), x + 86 - (i * 24), y + 109, matrixStack, buffers, light);
        }

//            for (int i = 0; i < 3; i++) {
//                f.renderString(sh, x + 110, y + 40 + (i * 25), -1, false, matrixStack.getLast().getMatrix(), buffers, true, -1, light);
//            }

//        gr.renderString(String.valueOf(known.charAt(0)), 0.0F, 0.0F, -1, false, matrixStack.getLast().getMatrix(), buffers, false, Integer.MIN_VALUE, light);
//        gr.renderString(String.valueOf(known.charAt(4)), 20.0F, 0.0F, -1, false, matrixStack.getLast().getMatrix(), buffers, false, Integer.MIN_VALUE, light);

        matrixStack.pop();
    }

    private void renderGlyph(String s, float x, float y, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
        FontRenderer f;
        f = s.equals("?") ? fr : gr;
        f.renderString(s, x, y, -1, false, matrixStack.getLast().getMatrix(), buffers, false, 0xFF23183a, light);
    }

}

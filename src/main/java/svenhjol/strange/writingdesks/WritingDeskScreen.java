package svenhjol.strange.writingdesks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import svenhjol.strange.Strange;

@Environment(EnvType.CLIENT)
public class WritingDeskScreen extends HandledScreen<WritingDeskScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(Strange.MOD_ID, "textures/gui/writing_desk.png");

    public WritingDeskScreen(WritingDeskScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        // much copypasta from GrindstoneScreen
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // the rest is unique
        WritingDeskScreenHandler handler = this.handler;
        if (!handler.getSlot(0).hasStack()
            || !handler.getSlot(1).hasStack()
            || !handler.getSlot(2).hasStack()
            || !handler.getSlot(3).hasStack()
        ) {
            this.drawTexture(matrices, i + 86, j + 32, this.backgroundWidth, 0, 28, 21);
        }
    }
}

package svenhjol.strange.writingdesks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.RunestoneHelper;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class WritingDeskScreen extends HandledScreen<WritingDeskScreenHandler> {
    private static final Identifier SGA_TEXTURE = new Identifier("minecraft", "alt");
    private static final Identifier TEXTURE = new Identifier(Strange.MOD_ID, "textures/gui/writing_desk.png");
    private static final Style SGA_STYLE = Style.EMPTY.withFont(SGA_TEXTURE);
    private int updateTicks = 0;

    private List<Integer> lastRunes = new ArrayList<>();
    private List<Integer> lastDiscovered = new ArrayList<>();

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
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);

        WritingDeskScreenHandler handler = this.handler;
        Slot slot0 = handler.getSlot(0);
        Slot slot1 = handler.getSlot(1);

        // draw the nope arrow
        if (!slot0.hasStack() || !slot1.hasStack())
            this.drawTexture(matrices, i + 99, j + 32, this.backgroundWidth, 0, 28, 21);

        if (this.client == null || this.client.player == null)
            return;

        // poll client data every half second
        if (updateTicks++ >= 10) {
            updateTicks = 0;
            lastRunes = new ArrayList<>();
            lastDiscovered = new ArrayList<>();
            BlockPos pos = handler.getPosFromStack(slot0.getStack(), this.client.world);

            if (pos != null)
                lastRunes = RunestoneHelper.getRunesFromBlockPos(pos, 6);

            lastDiscovered = RunestoneHelper.getDiscoveredRunes(this.client.player);
        }

        if (!slot0.hasStack())
            return;

        if (lastRunes.size() < 6)
            return;

        // render the rune grid

        int index = 0;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 2; y++) {
                int rune = lastRunes.get(index++);
                String runeChar = Character.toString((char)(rune + 97));
                boolean isKnownRune = this.client.player.isCreative() || lastDiscovered.contains(rune);

                Text runeText = new LiteralText(runeChar).fillStyle(SGA_STYLE);
                Text questionText = new LiteralText("?");
                this.textRenderer.draw(matrices, isKnownRune ? runeText : questionText, (float)(this.width / 2) - 30 + (x * 12), (float)(this.height / 2) - 51 + (y * 14), isKnownRune ? 0x6f654f : 0xdbc79c);
            }
        }

    }
}

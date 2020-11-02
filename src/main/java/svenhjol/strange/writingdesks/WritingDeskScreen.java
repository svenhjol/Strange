package svenhjol.strange.writingdesks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.Strange;
import svenhjol.strange.module.RunicTablets;
import svenhjol.strange.runestones.RunestoneHelper;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class WritingDeskScreen extends HandledScreen<WritingDeskScreenHandler> {
    private static final Identifier SGA_TEXTURE = new Identifier("minecraft", "alt");
    private static final Identifier TEXTURE = new Identifier(Strange.MOD_ID, "textures/gui/writing_desk.png");
    private static final Style SGA_STYLE = Style.EMPTY.withFont(SGA_TEXTURE);
    private int updateTicks = 0;
    private boolean satisfied = false;

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
        if (this.client == null || this.client.player == null)
            return;

        PlayerEntity player = this.client.player;
        World world = this.client.world;
        int midWidth = this.width / 2;
        int midHeight = this.height / 2;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if (!satisfied)
            this.drawTexture(matrices, x + 99, y + 32, this.backgroundWidth, 0, 28, 21);

        WritingDeskScreenHandler handler = this.handler;
        Slot slot0 = handler.getSlot(0);
        Slot slot1 = handler.getSlot(1);

        satisfied = slot0.hasStack() && slot1.hasStack();

        // if the player doesn't have enough XP, show the text for this
        if (slot0.hasStack() && !player.isCreative() && player.experienceLevel < RunicTablets.requiredXpLevels) {
            TranslatableText text = new TranslatableText("writingdesk.strange.not_enough_xp", RunicTablets.requiredXpLevels);
            int y1 = (midHeight - 23);
            int y2 = y1 + 10;
            int x1 = (midWidth + 80) - 8 - this.textRenderer.getWidth(text) - 2;
            int x2 = (midWidth + 80) - 8;
            fill(matrices, x1 - 2, y1 - 2, x2, y2, 0x4F000000);
            this.textRenderer.drawWithShadow(matrices, text, x1, y1, 0xFF6060);
            satisfied = false;
        }

        // poll client data every half second
        if (updateTicks++ >= 10) {
            updateTicks = 0;
            lastRunes = new ArrayList<>();
            lastDiscovered = new ArrayList<>();
            BlockPos pos = handler.getPosFromStack(slot0.getStack(), world);

            if (pos != null)
                lastRunes = RunestoneHelper.getRunesFromBlockPos(pos, 6);

            lastDiscovered = RunestoneHelper.getDiscoveredRunes(player);
        }

        // conditions for not rendering the grid
        if (!slot0.hasStack() || lastRunes.size() < 6) {
            satisfied = false;
            return;
        }

        // render the rune grid
        int index = 0;
        for (int ix = 0; ix < 3; ix++) {
            for (int iy = 0; iy < 2; iy++) {
                int rune = lastRunes.get(index++);
                String runeChar = Character.toString((char)(rune + 97));
                boolean isKnownRune = player.isCreative() || lastDiscovered.contains(rune);
                if (!isKnownRune)
                    satisfied = false;

                Text runeText = new LiteralText(runeChar).fillStyle(SGA_STYLE);
                Text questionText = new LiteralText("?");
                this.textRenderer.draw(matrices, isKnownRune ? runeText : questionText, (float)(this.width / 2) - 30 + (ix * 12), (float)(this.height / 2) - 51 + (iy * 14), isKnownRune ? 0x6f654f : 0xdbc79c);
            }
        }
    }
}

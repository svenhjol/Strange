package svenhjol.strange.runicaltars;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.RunestoneHelper;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RunicAltarScreen extends HandledScreen<RunicAltarScreenHandler> {
    private static final Identifier SGA_TEXTURE = new Identifier("minecraft", "alt");
    private static final Identifier TEXTURE = new Identifier(Strange.MOD_ID, "textures/gui/runic_altar.png");
    private static final Style SGA_STYLE = Style.EMPTY.withFont(SGA_TEXTURE);

    private List<Integer> requiredRunes = new ArrayList<>();
    private List<Integer> learnedRunes = new ArrayList<>();

    private int playerRuneCheckTicks = 0;

    public RunicAltarScreen(RunicAltarScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        handler.addListener(new ScreenHandlerListener() {
            @Override
            public void onHandlerRegistered(ScreenHandler handler, DefaultedList<ItemStack> stacks) {
                // nah
            }

            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
                if (client != null && slotId == 0) {
                    BlockPos pos = RunestoneHelper.getBlockPosFromItemStack(client.world, handler.getSlot(slotId).getStack());
                    requiredRunes = pos != null ? RunestoneHelper.getRunesFromBlockPos(pos, 8) : new ArrayList<>();
                    tryUpdatePlayerLearnedRunes();
                }
            }

            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
                // nah
            }
        });
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
        int midWidth = this.width / 2;
        int midHeight = this.height / 2;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        RunicAltarScreenHandler handler = this.handler;
        Slot slot0 = handler.getSlot(0);
        if (!slot0.hasStack())
            return;

        // poll player runes
        if (++playerRuneCheckTicks == 15) {
            playerRuneCheckTicks = 0;
//            tryUpdatePlayerLearnedRunes();
        }

        // render the rune grid
        float sx = (float) (this.width / 2) - 9;
        float sy = (float) (this.height / 2) - 67;

        renderGlyph(0, sy, sx, matrices);
        renderGlyph(1, sy, sx + 13, matrices);
        renderGlyph(2, sy + 14, sx + 29, matrices);
        renderGlyph(3, sy + 30, sx + 29, matrices);
        renderGlyph(4, sy + 45, sx + 13, matrices);
        renderGlyph(5, sy + 45, sx, matrices);
        renderGlyph(6, sy + 30, sx - 16, matrices);
        renderGlyph(7, sy + 14, sx - 16, matrices);
    }

    private void renderGlyph(int index, float y, float x, MatrixStack matrices) {
        Text knownText = new LiteralText("!");
        Text unknownText = new LiteralText("?");

        boolean isKnownRune;

        if (requiredRunes.size() > index) {
            int rune = requiredRunes.get(index);
            String runeChar = Character.toString((char) (rune + 97));
            knownText = new LiteralText(runeChar).fillStyle(SGA_STYLE);
            isKnownRune = (client != null && client.player != null && client.player.isCreative()) || learnedRunes.contains(rune);
        } else {
            isKnownRune = false;
        }

        this.textRenderer.draw(matrices, isKnownRune ? knownText : unknownText, x, y, isKnownRune ? 0x5f553f : 0xdbc79c);
    }

    private void tryUpdatePlayerLearnedRunes() {
        if (client != null && client.player != null)
            learnedRunes = RunestoneHelper.getLearnedRunes(client.player);
    }
}

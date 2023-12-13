package svenhjol.strange.feature.runestones;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.helper.TextHelper;

public class RunestoneHudRenderer {
    static final int MAX_FADE_TICKS = 200;
    static final int MIN_BACKOFF_TICKS = 5;
    static final int MAX_BACKOFF_TICKS = 10;

    boolean isValid = false;
    boolean textShadow;
    int nameColor;
    int runeNameColor;
    int discoveredColor;
    int fadeInSpeed;
    int fadeOutSpeed;
    String runeName = "";
    String name = "";
    String discovered = "";
    int ticks = 0;
    int ticksFade = 0;
    int ticksBackoff = MIN_BACKOFF_TICKS;

    public RunestoneHudRenderer() {
        fadeInSpeed = 4;
        fadeOutSpeed = 10;
        runeNameColor = 0xbfaf9f;
        nameColor = 0xf8f8ff;
        discoveredColor = 0xafbfcf;
        textShadow = true;
    }

    public void tick(Player player) {
        if (ticks % ticksBackoff == 0) {
            var nowValid = isValid(player);
            if (isValid && !nowValid) {
                isValid = false;
                ticksFade = MAX_FADE_TICKS;
                ticksBackoff = MIN_BACKOFF_TICKS;
                ticks = 0;
            }
            if (!isValid && nowValid) {
                isValid = true;
                ticksFade = 1;
                ticks = 0;
            }
            if (!isValid && ticksBackoff < MAX_BACKOFF_TICKS) {
                ticksBackoff += 5;
            }
        }

        if (ticksFade < 0) {
            ticksFade = 0;
        }

        ticks++;
    }

    public void renderLabel(GuiGraphics guiGraphics, float tickDelta) {
        int y = 70;
        int lineHeight = 14;
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        if (ticksFade == 0) return;
        if (name == null || name.isEmpty()) return;

        var isCreative = minecraft.player.getAbilities().instabuild;
        var gui = minecraft.gui;
        var font = minecraft.font;
        var midX = (int)(gui.screenWidth / 2.0F);
        var alpha = Math.max(4, Math.min(MAX_FADE_TICKS, ticksFade)) << 24 & 0xff000000;

        int nameLength;
        int runeNameLength;
        int discoveredLength;
        MutableComponent name;
        MutableComponent runeName;
        MutableComponent discovered;

        runeName = TextHelper.translatable(this.runeName).withStyle(RunestonesClient.ILLAGER_GLYPHS_STYLE);
        runeNameLength = runeName.getString().length() * 5;

        if (isCreative && this.discovered == null) {
            this.discovered = "Creative mode";
        }

        if (this.discovered != null) {
            name = TextHelper.translatable(this.name);
            nameLength = name.getString().length() * 5;
            discovered = TextHelper.translatable("gui.strange.runestone.discovered_by", this.discovered);
            discoveredLength = discovered.getString().length() * 5;
        } else {
            name = TextHelper.literal("???");
            nameLength = name.getString().length() * 5;
            discovered = Component.empty();
            discoveredLength = 0;
        }

        if (nameLength > 0) {
            int lx = (int) (midX - (float) (nameLength / 2) - 2) + 3;
            guiGraphics.drawString(font, name, lx, y, nameColor | alpha, textShadow);
            y += lineHeight;
        }

        if (discoveredLength > 0) {
            int lx = (int) (midX - (float) (discoveredLength / 2) - 2);
            guiGraphics.drawString(font, discovered, lx, y, discoveredColor | alpha, textShadow);
            y += lineHeight;
        }

        if (runeNameLength > 0) {
            int lx = (int) (midX - (float) (runeNameLength / 2) - 2) - 3;
            guiGraphics.drawString(font, runeName, lx, y, runeNameColor | alpha, textShadow);
            y += lineHeight;
        }

        doFadeTicks();
    }

    public boolean isValid(Player player) {
        var level = player.level();
        var lookedAt = RunestoneHelper.getBlockLookedAt(player);
        var isCreative = player.getAbilities().instabuild;

        if (level.getBlockEntity(lookedAt) instanceof RunestoneBlockEntity runestone) {
            if (runestone.destination == null) {
                return false;
            }

            // Always generate the runic name for this location.
            this.runeName = RunestoneHelper.getRunicName(runestone.type, runestone.destination);

            if (isCreative || runestone.discovered != null) {
                this.discovered = runestone.discovered;
                this.name = RunestoneHelper.getLocaleKey(runestone.type, runestone.destination);
            } else {
                this.discovered = null;
            }

            return true;
        }

        return false;
    }

    private void doFadeTicks() {
        if (isValid && ticksFade > 0 && ticksFade < MAX_FADE_TICKS) {
            ticksFade += fadeInSpeed;
        } else if (!isValid && ticksFade > 0) {
            ticksFade -= fadeOutSpeed;
        }
    }
}

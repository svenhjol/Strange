package svenhjol.strange.feature.runestones;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.travel_journal.TravelJournal;

public class RunestoneHudRenderer {
    static final int MAX_FADE_TICKS = 200;
    static final int MIN_BACKOFF_TICKS = 5;
    static final int MAX_BACKOFF_TICKS = 10;

    boolean isValid = false;
    boolean textShadow;
    int nameColor;
    int runesColor;
    int discoveredColor;
    int fadeInSpeed;
    int fadeOutSpeed;
    MutableComponent runes;
    MutableComponent name;
    MutableComponent discovered;
    int ticks = 0;
    int ticksFade = 0;
    int ticksBackoff = MIN_BACKOFF_TICKS;

    public RunestoneHudRenderer() {
        fadeInSpeed = 4;
        fadeOutSpeed = 10;
        runesColor = 0xbfaf9f;
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

        if (ticksFade == 0) return;

        var gui = minecraft.gui;
        var font = minecraft.font;
        var midX = (int)(gui.screenWidth / 2.0f);
        var alpha = Math.max(4, Math.min(MAX_FADE_TICKS, ticksFade)) << 24 & 0xff000000;

        int nameLength;
        int runesLength;
        int discoveredLength;

        runesLength = runes.getString().length() * 5;
        nameLength = name.getString().length() * 5;
        discoveredLength = discovered.getString().length() * 5;

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

        if (runesLength > 0) {
            int lx = (int) (midX - (float) (runesLength / 2) - 2) - 3;
            guiGraphics.drawString(font, runes, lx, y, runesColor | alpha, textShadow);
            y += lineHeight;
        }

        doFadeTicks();
    }

    public boolean isValid(Player player) {
        var level = player.level();
        var lookedAt = RunestoneHelper.getBlockLookedAt(player);
        var isCreative = player.getAbilities().instabuild;

        if (level.getBlockEntity(lookedAt) instanceof RunestoneBlockEntity runestone) {
            this.discovered = Component.empty();

            if (runestone.destination == null) {
                return false;
            }

            this.runes = TextHelper.literal(RunestoneHelper.getRunicName(runestone.type, runestone.destination))
                .withStyle(RunestonesClient.ILLAGER_GLYPHS_STYLE);

            if (runestone.discovered == null && isCreative) {
                this.name = TextHelper.translatable(RunestoneHelper.getLocaleKey(runestone.type, runestone.destination));
                this.discovered = TextHelper.translatable("gui.strange.runestone.discovered_by", "Creative mode");
                return true;
            }

            if (runestone.discovered != null) {
                // Show the "Discovered by" message.
                this.name = TextHelper.translatable(RunestoneHelper.getLocaleKey(runestone.type, runestone.destination));
                this.discovered = TextHelper.translatable("gui.strange.runestone.discovered_by", runestone.discovered);
            } else {
                // If the player has learned the runes for this destination, show the name with a "?" at the end.
                var learned = TravelJournal.getLearned(player);
                var hasLearned = learned.isPresent() && learned.get().learned(runestone.destination);

                if (hasLearned) {
                    this.name = TextHelper.translatable("gui.strange.runestone.unsure", TextHelper.translatable(RunestoneHelper.getLocaleKey(runestone.type, runestone.destination)));
                } else {
                    this.name = TextHelper.translatable("gui.strange.runestone.unknown");
                }
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

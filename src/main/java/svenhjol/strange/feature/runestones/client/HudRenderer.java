package svenhjol.strange.feature.runestones.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.feature.runestones.RunestonesClient;
import svenhjol.strange.feature.runestones.common.RunestoneBlockEntity;

import javax.annotation.Nullable;

public class HudRenderer extends FeatureHolder<RunestonesClient> {
    private static final int MAX_FADE_TICKS = 200;
    private static final int MIN_BACKOFF_TICKS = 5;
    private static final int MAX_BACKOFF_TICKS = 10;

    private final int nameColor;
    private final int runesColor;
    private final int discoveredColor;
    private final int fadeInSpeed;
    private final int fadeOutSpeed;
    private boolean isValid = false;
    private boolean textShadow;

    @Nullable
    private ItemStack sacrifice;

    private MutableComponent runes;
    private MutableComponent name;
    private MutableComponent discovered;
    private MutableComponent activateWith;

    private int ticks = 0;
    private int ticksFade = 0;
    private int ticksBackoff = MIN_BACKOFF_TICKS;

    public HudRenderer(RunestonesClient feature) {
        super(feature);

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

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int y = 50;
        int lineHeight = 14;
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();

        if (ticksFade == 0) return;

        var font = minecraft.font;
        var midX = (int)(window.getGuiScaledWidth() / 2.0f);
        var alpha = Math.max(4, Math.min(MAX_FADE_TICKS, ticksFade)) << 24 & 0xff000000;
        var scale = Math.max(0f, Math.min(1.0f, (ticksFade / (float) MAX_FADE_TICKS)));

        int nameStringLength;
        int runesStringLength;
        int discoveredStringLength;
        int activateWithStringLength;

        runesStringLength = runes.getString().length() * 5;
        nameStringLength = name.getString().length() * 5;
        discoveredStringLength = discovered.getString().length() * 5;
        activateWithStringLength = activateWith.getString().length() * 5; // reserve space for the item!

        if (nameStringLength > 0) {
            y += lineHeight;
            int lx = (int) (midX - (float) (nameStringLength / 2) - 2) + 3;
            guiGraphics.drawString(font, name, lx, y, nameColor | alpha, textShadow);
        }

        if (discoveredStringLength > 0) {
            y += lineHeight;
            int lx = (int) (midX - (float) (discoveredStringLength / 2) - 2);
            guiGraphics.drawString(font, discovered, lx, y, discoveredColor | alpha, textShadow);
        }

        if (runesStringLength > 0) {
            y += lineHeight;
            int lx = (int) (midX - (float) (runesStringLength / 2) - 2) - 3;
            guiGraphics.drawString(font, runes, lx, y, runesColor | alpha, textShadow);
        }

        if (activateWithStringLength > 0 && sacrifice != null) {
            y += lineHeight;
            int lx = (int) (midX - (float) (activateWithStringLength / 2) - 2);
            guiGraphics.drawString(font, activateWith, lx, y, nameColor | alpha, textShadow);

            int ix = midX + (activateWithStringLength / 2) - 6;
            int iy = y - 4;
            feature().handlers.renderScaledGuiItem(sacrifice, guiGraphics, ix, iy, scale, scale);
        }

        doFadeTicks();
    }

    public boolean isValid(Player player) {
        var level = player.level();
        var handlers = feature().handlers;
        var lookedAt = handlers.lookingAtBlock(player);
        var isCreative = player.getAbilities().instabuild;

        if (level.getBlockEntity(lookedAt) instanceof RunestoneBlockEntity runestone) {
            this.discovered = Component.empty();

            if (runestone.location == null) {
                return false;
            }

            if (runestone.isActivated()) {
                this.sacrifice = null;
                this.activateWith = Component.empty();
            } else {
                this.sacrifice = runestone.sacrifice;
                this.activateWith = Component.translatable("gui.strange.runestone.activate_with");
            }

            this.runes = Component.literal(handlers.runicName(runestone.location))
                .withStyle(feature().registers.runeFont);

            if (runestone.discovered == null && isCreative) {
                this.name = Component.translatable(handlers.localeKey(runestone.location));
                this.discovered = Component.translatable("gui.strange.runestone.discovered_by", "Creative mode");
                return true;
            }

            if (runestone.discovered != null) {
                // Show the "Discovered by" message.
                this.name = Component.translatable(handlers.localeKey(runestone.location));
                this.discovered = Component.translatable("gui.strange.runestone.discovered_by", runestone.discovered);
            } else {
                // If the player has learned the runes for this destination, show the name with a "?" at the end.
                // TODO: learning rune locations.
//                var hasLearned = LearnedRunes.getLearned(player).hasLearned(runestone.location);
//
//                if (hasLearned) {
//                    this.name = Component.translatable("gui.strange.runestone.unsure", Component.translatable(RunestoneHelper.getLocaleKey(runestone.location)));
//                } else {
                    this.name = Component.translatable("gui.strange.runestone.unknown");
//                }
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

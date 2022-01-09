package svenhjol.strange.module.runes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import svenhjol.strange.init.StrangeFonts;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Runes;

public class RuneStringRenderer {
    private int left;
    private int top;
    private final int xOffset;
    private final int yOffset;
    private final int xMax;
    private final int yMax;
    private final String unknownRune;

    private int knownColor;
    private int unknownColor;
    private boolean withShadow;

    public RuneStringRenderer(int left, int top, int xOffset, int yOffset, int xMax, int yMax) {
        this.left = left;
        this.top = top;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xMax = xMax;
        this.yMax = yMax;
        this.knownColor = 0x997755;
        this.unknownColor = 0xC0B0A0;
        this.withShadow = false;
        this.unknownRune = String.valueOf(Runes.UNKNOWN_RUNE);
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return xMax * xOffset;
    }

    public int getHeight() {
        return yMax * yOffset;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setWithShadow(boolean withShadow) {
        this.withShadow = withShadow;
    }

    public void setKnownColor(int knownColor) {
        this.knownColor = knownColor;
    }

    public void setUnknownColor(int unknownColor) {
        this.unknownColor = unknownColor;
    }

    public void render(PoseStack poseStack, Font font, String runes) {
        var journal = JournalsClient.getJournal().orElse(null);
        if (journal == null) return;

        // Convert the input string according to the runes that the player knows.
        String revealed = RuneHelper.revealRunes(runes, JournalHelper.getLearnedRunes(journal));

        int index = 0;

        for (int y = 0; y < yMax; y++) {
            for (int x = 0; x < xMax; x++) {
                if (index < revealed.length()) {
                    Component rune;
                    int color;

                    String s = String.valueOf(revealed.charAt(index));

                    if (s.equals(unknownRune)) {
                        rune = new TextComponent(unknownRune);
                        color = unknownColor;
                    } else {
                        rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
                        color = knownColor;
                    }

                    int xo = left + (x * xOffset);
                    int yo = top + (y * yOffset);

                    if (withShadow) {
                        font.drawShadow(poseStack, rune, xo, yo, color);
                    } else {
                        font.draw(poseStack, rune, xo, yo, color);
                    }
                }

                index++;
            }
        }
    }
}

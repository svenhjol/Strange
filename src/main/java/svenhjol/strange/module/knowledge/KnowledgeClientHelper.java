package svenhjol.strange.module.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.init.StrangeFonts;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.Journals;

public class KnowledgeClientHelper {
    public static void renderRunesString(@Nullable Minecraft client, PoseStack poseStack, String runes, int left, int top, int xOffset, int yOffset, int xMax, int yMax, int knownColor, int unknownColor, boolean withShadow) {
        if (client == null || client.player == null) return;

        Player player = client.player;
        if (Journals.getJournalData(player).isEmpty()) return;

        JournalData journal = Journals.getJournalData(player).get();
        String knownRuneString = KnowledgeHelper.convertRunesWithLearnedRunes(runes, journal.getLearnedRunes());
        int index = 0;

        for (int y = 0; y < yMax; y++) {
            for (int x = 0; x < xMax; x++) {
                if (index < knownRuneString.length()) {
                    Component rune;
                    int color;

                    String s = String.valueOf(knownRuneString.charAt(index));
                    if (s.equals(KnowledgeHelper.UNKNOWN)) {
                        rune = new TextComponent(KnowledgeHelper.UNKNOWN);
                        color = unknownColor;
                    } else {
                        rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
                        color = knownColor;
                    }

                    int xo = left + (x * xOffset);
                    int yo = top + (y * yOffset);

                    if (withShadow) {
                        client.font.drawShadow(poseStack, rune, xo, yo, color);
                    } else {
                        client.font.draw(poseStack, rune, xo, yo, color);
                    }
                }
                index++;
            }
        }
    }
}

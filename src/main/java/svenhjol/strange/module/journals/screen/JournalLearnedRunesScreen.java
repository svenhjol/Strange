package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.JournalsData;
import svenhjol.strange.module.knowledge.Knowledge;

import java.util.List;

public class JournalLearnedRunesScreen extends BaseJournalScreen {
    protected JournalsData playerData = null;

    protected JournalLearnedRunesScreen() {
        super(new TranslatableComponent("gui.strange.journal.learned_runes.title"));

        // get reference to player data. Should have been cached on client side when journal opened
        JournalsClient.getPlayerData().ifPresent(data -> this.playerData = data);

        // add a back button at the bottom
        bottomButtons.add(0, new ButtonDefinition(b -> knowledge(),
            new TranslatableComponent("gui.strange.journal.go_back")));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        if (playerData == null) {
            // show an error if the runes can't be loaded
            centeredText(poseStack, font, new TranslatableComponent("gui.strange.journal.runes_load_error"), (width / 2), titleY + 14, errorColor);
            return;
        }

        int mid = this.width / 2;
        int left = mid + 22;
        int top = 46;
        int xOffset = 22;
        int yOffset = 18;
        int index = 0;

        List<Integer> learnedRunes = playerData.getLearnedRunes();

        for (int sx = 0; sx <= 4; sx++) {
            for (int sy = 0; sy < 7; sy++) {
                if (index < Knowledge.NUM_RUNES) {
                    boolean knownRune = learnedRunes.contains(index);
                    Component runeText;
                    int color;

                    if (knownRune) {
                        String runeChar = Character.toString((char) (index + 97));
                        runeText = new TextComponent(runeChar).withStyle(SGA_STYLE);
                        color = KNOWN_COLOR;
                    } else {
                        runeText = new TextComponent("?");
                        color = UNKNOWN_COLOR;
                    }

                    font.draw(poseStack, runeText, left + (sx * xOffset), top + (sy * yOffset), color);
                }
                index++;
            }
        }
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, page1Center, titleY, titleColor);
    }
}

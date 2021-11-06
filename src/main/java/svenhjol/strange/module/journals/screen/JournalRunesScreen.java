package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeClient;

import java.util.List;

public class JournalRunesScreen extends JournalScreen {
    public static final Component RUNES_LOAD_ERROR = new TranslatableComponent("gui.strange.journal.runes_load_error");
    protected JournalRunesScreen() {
        super(LEARNED_RUNES);

        // add a back button at the bottom
        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> knowledge(), GO_BACK));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        if (journal == null) {
            // show an error if the runes can't be loaded
            centeredText(poseStack, font, RUNES_LOAD_ERROR, midX, titleY + 14, errorColor);
            return;
        }

        int left = midX + 22;
        int top = 46;
        int xOffset = 22;
        int yOffset = 18;

        List<Integer> learnedRunes = journal.getLearnedRunes();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < Knowledge.NUM_RUNES; i++) {
            if (learnedRunes.contains(i)) {
                builder.append((char)(i + 97));
            } else {
                builder.append("?");
            }
        }

        String knownRunes = builder.toString();
        KnowledgeClient.renderRunesString(minecraft, poseStack, knownRunes, left, top, xOffset, yOffset, 4, 8, knownColor, unknownColor, false);
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, page1Center, titleY, titleColor);
    }
}

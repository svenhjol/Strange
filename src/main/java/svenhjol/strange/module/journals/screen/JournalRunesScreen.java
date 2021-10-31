package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeClient;

import java.util.List;

public class JournalRunesScreen extends BaseJournalScreen {
    protected JournalRunesScreen() {
        super(new TranslatableComponent("gui.strange.journal.learned_runes.title"));

        // add a back button at the bottom
        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> knowledge(),
            new TranslatableComponent("gui.strange.journal.go_back")));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        if (journal == null) {
            // show an error if the runes can't be loaded
            centeredText(poseStack, font, new TranslatableComponent("gui.strange.journal.runes_load_error"), midX, titleY + 14, errorColor);
            return;
        }

        int left = midX + 22;
        int top = 46;
        int xOffset = 22;
        int yOffset = 18;
        int index = 0;

        List<Integer> learnedRunes = journal.getLearnedRunes();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < Knowledge.NUM_RUNES; i++) {
            if (learnedRunes.contains(i)) {
                builder.append((char)(i + 97));
            } else {
                builder.append("WANK");
            }
        }

        String knownRunes = builder.toString();
        KnowledgeClient.renderRunesString(minecraft, poseStack, knownRunes, left, top, xOffset, yOffset, 4, 8, KNOWN_COLOR, UNKNOWN_COLOR, false);
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, page1Center, titleY, titleColor);
    }
}

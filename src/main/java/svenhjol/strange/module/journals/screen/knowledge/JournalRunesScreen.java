package svenhjol.strange.module.journals.screen.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.helper.Journal2Helper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.runes.Runes;

import java.util.List;

public class JournalRunesScreen extends JournalScreen {
    public static final Component RUNES_LOAD_ERROR = new TranslatableComponent("gui.strange.journal.runes_load_error");
    public JournalRunesScreen() {
        super(LEARNED_RUNES);
        Journals2Client.tracker.setPage(Journals.Page.RUNES);
    }

    @Override
    protected void init() {
        super.init();
        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> knowledge(), GO_BACK));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        int page1Left = midX - 82;
        int page2Left = midX + 33;
        int top = 49;
        int xOffset = 42;
        int yOffset = 20;

        List<Integer> learnedRunes = Journal2Helper.getLearnedRunes();
        StringBuilder page1 = new StringBuilder();
        StringBuilder page2 = new StringBuilder();

        int perPage = Runes.NUM_RUNES / 2;

        for (int i = 0; i < perPage; i++) {
            if (learnedRunes.contains(i)) {
                page1.append((char)(i + 97));
            } else {
                page1.append("?");
            }
        }

        for (int i = perPage; i < Knowledge.NUM_RUNES; i++) {
            if (learnedRunes.contains(i)) {
                page2.append((char)(i + 97));
            } else {
                page2.append("?");
            }
        }

        renderRunesString(poseStack, page1.toString(), page1Left, top, xOffset, yOffset, 4, 4, false);
        renderRunesString(poseStack, page2.toString(), page2Left, top, xOffset, yOffset, 4, 4, false);
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, titleX, titleY, titleColor);
    }
}

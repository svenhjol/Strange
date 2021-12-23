package svenhjol.strange.module.journals.screen.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.runes.Runes;
import svenhjol.strange.module.runes.client.RuneStringRenderer;

import java.util.List;

public class JournalRunesScreen extends JournalScreen {
    private RuneStringRenderer runeStringRenderer;

    public JournalRunesScreen() {
        super(LEARNED_RUNES);
        JournalsClient.tracker.setPage(PageTracker.Page.RUNES);
    }

    @Override
    protected void init() {
        super.init();
        runeStringRenderer = new RuneStringRenderer(0, 49, 42, 20, 2, 7);
        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> knowledge(), GO_BACK));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        var journal = JournalsClient.getJournal().orElse(null);
        if (journal == null) return;

        List<Integer> learnedRunes = JournalHelper.getLearnedRunes(journal);
        List<StringBuilder> pages = List.of(
            new StringBuilder(),
            new StringBuilder()
        );

        boolean render = false;
        for (StringBuilder page : pages) {
            for (int i = 0; i < Runes.NUM_RUNES; i++) {
                if (i % 2 == 0) render = !render;

                if (render) {
                    if (learnedRunes.contains(i)) {
                        page.append((char) (i + 97));
                    } else {
                        page.append(Runes.UNKNOWN_RUNE);
                    }
                }
            }
        }

        runeStringRenderer.setLeft(midX - 82);
        runeStringRenderer.render(poseStack, font, pages.get(0).toString());

        runeStringRenderer.setLeft(midX + 33);
        runeStringRenderer.render(poseStack, font, pages.get(1).toString());
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, titleX, titleY, titleColor);
    }
}

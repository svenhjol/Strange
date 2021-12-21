package svenhjol.strange.module.journals.screen.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.helper.GuiHelper.ButtonDefinition;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals.PageTracker;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class JournalKnowledgeScreen extends JournalScreen {
    protected List<ButtonDefinition> pageButtons;

    public JournalKnowledgeScreen() {
        this(KNOWLEDGE);
    }

    public JournalKnowledgeScreen(Component component) {
        super(component);

        pageButtons = Arrays.asList(
            new ButtonDefinition(b -> runes(), LEARNED_RUNES),
            new ButtonDefinition(b -> biomes(), LEARNED_BIOMES),
            new ButtonDefinition(b -> structures(), LEARNED_STRUCTURES),
            new ButtonDefinition(b -> dimensions(), LEARNED_DIMENSIONS)
        );

        JournalsClient.tracker.setPage(PageTracker.Page.KNOWLEDGE);
    }

    @Override
    protected void firstRender(PoseStack poseStack) {
        super.firstRender(poseStack);

        int buttonWidth = 105;
        int buttonHeight = 20;
        int x = midX - 50;
        int y = 40;
        int yOffset = 24;

        GuiHelper.addButtons(this, width, font, pageButtons, x, y, 0, yOffset, buttonWidth, buttonHeight);
    }
}

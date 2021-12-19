package svenhjol.strange.module.journals.screen.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;
import svenhjol.strange.module.journals2.paginator.QuestPaginator;
import svenhjol.strange.module.quests.QuestsClient;

@SuppressWarnings("ConstantConditions")
public class JournalQuestsScreen extends JournalScreen {
    private QuestPaginator paginator;

    public JournalQuestsScreen() {
        super(QUESTS);
    }

    @Override
    protected void init() {
        super.init();

        var quests = QuestsClient.quests;
        quests.forEach(q -> q.update(minecraft.player));

        paginator = new QuestPaginator(quests);
        paginator.init(this, offset, midX, 40, newOffset -> {
            offset = newOffset;
            init(minecraft, width, height);
        });

        Journals2Client.tracker.setPage(PageTracker.Page.QUESTS, offset);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        paginator.render(poseStack, itemRenderer, font);
    }
}

package svenhjol.strange.module.journals.screen.quest;

import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.journals.paginator.QuestPaginator;
import svenhjol.strange.module.journals.screen.JournalPaginatedScreen;
import svenhjol.strange.module.journals.screen.JournalResources;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestsClient;

import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class JournalQuestsScreen extends JournalPaginatedScreen<Quest> {
    public JournalQuestsScreen() {
        super(JournalResources.QUESTS);
    }

    @Override
    protected Consumer<Quest> onClick() {
        return quest -> minecraft.setScreen(new JournalQuestScreen(quest));
    }

    @Override
    public QuestPaginator getPaginator() {
        var quests = QuestsClient.quests;
        quests.forEach(q -> q.update(minecraft.player));
        return new QuestPaginator(quests);
    }

    @Override
    protected void setViewedPage() {
        JournalsClient.tracker.setPage(PageTracker.Page.QUESTS, offset);
    }
}

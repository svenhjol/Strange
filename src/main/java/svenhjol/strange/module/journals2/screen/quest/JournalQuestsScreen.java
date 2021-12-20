package svenhjol.strange.module.journals2.screen.quest;

import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;
import svenhjol.strange.module.journals2.paginator.QuestPaginator;
import svenhjol.strange.module.journals2.screen.JournalPaginatedScreen;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestsClient;

import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class JournalQuestsScreen extends JournalPaginatedScreen<Quest> {
    public JournalQuestsScreen() {
        super(QUESTS);
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
        Journals2Client.tracker.setPage(PageTracker.Page.QUESTS, offset);
    }
}

package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.GuiGraphics;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestsHelper;
import svenhjol.strange.feature.quests.QuestsNetwork.AbandonQuest;
import svenhjol.strange.feature.quests.client.QuestButtons.AbandonShortcutButton;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.client.BaseTravelJournalScreen;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.BackButton;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.CloseButton;

public class QuestScreen extends BaseTravelJournalScreen {
    protected Quest quest;
    protected BaseQuestRenderer<Quest> renderer;

    public QuestScreen(Quest quest) {
        super(QuestsHelper.makeQuestTitle(quest));
        this.quest = quest;
        PageTracker.quest = quest;
        PageTracker.Screen.QUEST.set();
        renderer = quest.type().makeRenderer(quest);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new CloseButton(midX + 5,220, b -> onClose()));
        addRenderableWidget(new BackButton(midX - (BackButton.WIDTH + 5), 220, this::openQuests));
        renderer.setAbandonAction(b -> abandon());
        renderer.initSelectedActive(this);
        initShortcuts();
    }

    @Override
    protected void initShortcuts() {
        super.initShortcuts();
        addRenderableWidget(new AbandonShortcutButton(midX + 120, 161, b -> abandon()));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderer.renderSelectedActive(guiGraphics, mouseX, mouseY);
    }

    protected void abandon() {
        AbandonQuest.send(quest.id());
    }
}

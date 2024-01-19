package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.gui.GuiGraphics;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestHelper;
import svenhjol.strange.feature.quests.QuestsNetwork;
import svenhjol.strange.feature.quests.client.BaseQuestRenderer;
import svenhjol.strange.feature.travel_journal.PageTracker;

public class QuestScreen extends BaseTravelJournalScreen {
    protected Quest<?> quest;
    protected BaseQuestRenderer<Quest<?>> renderer;

    public QuestScreen(Quest<?> quest) {
        super(QuestHelper.makeQuestTitle(quest));
        this.quest = quest;
        PageTracker.quest = quest;
        PageTracker.Screen.QUEST.set();
        renderer = quest.type().makeRenderer(quest);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new TravelJournalButtons.CloseButton(midX + 5,220, b -> onClose()));
        addRenderableWidget(new TravelJournalButtons.BackButton(midX - (TravelJournalButtons.BackButton.WIDTH + 5), 220, this::openQuests));
        renderer.initSelectedActive(this, b -> QuestsNetwork.AbandonQuest.send(quest.id()));
        initShortcuts();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderer.renderSelectedActive(this, guiGraphics, mouseX, mouseY);
    }
}

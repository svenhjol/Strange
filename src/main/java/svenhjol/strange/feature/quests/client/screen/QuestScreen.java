package svenhjol.strange.feature.quests.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.quests.QuestsClient;
import svenhjol.strange.feature.quests.QuestsHelper;
import svenhjol.strange.feature.quests.QuestsNetwork.AbandonQuest;
import svenhjol.strange.feature.quests.client.QuestsButtons.AbandonShortcutButton;
import svenhjol.strange.feature.quests.client.renderer.BaseQuestRenderer;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.BackButton;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.CloseButton;
import svenhjol.strange.feature.travel_journal.client.screen.TravelJournalScreen;

public class QuestScreen extends TravelJournalScreen {
    protected Quest quest;
    protected BaseQuestRenderer<Quest> renderer;

    public QuestScreen(Quest quest) {
        super(QuestsHelper.makeQuestTitle(quest));
        this.quest = quest;
        renderer = quest.type().makeRenderer(quest);

        PageTracker.set(() -> this);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new CloseButton(midX + 5,220, b -> onClose()));
        addRenderableWidget(new BackButton(midX - (BackButton.WIDTH + 5), 220, QuestsClient::openQuestsScreen));
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

        checkValid();
    }

    protected void checkValid() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != null && !Quests.getPlayerQuests(minecraft.player).exists(this.quest)) {
            minecraft.setScreen(new QuestsScreen());
        }
    }

    protected void abandon() {
        AbandonQuest.send(quest.id());
    }
}

package svenhjol.strange.feature.quests.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import svenhjol.strange.feature.quests.Quest;
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
        this.renderer = quest.type().makeRenderer(quest);

        PageTracker.set(() -> new QuestScreen(quest));
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new CloseButton(midX + 5,220, b -> onClose()));
        addRenderableWidget(new BackButton(midX - (BackButton.WIDTH + 5), 220, QuestsClient::openQuestsScreen));

        renderer.setAbandonAction(b -> abandon());
        renderer.initSelectedActive(this);

        updateQuest();
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
        if (minecraft.player != null && !QuestsClient.getPlayerQuests(minecraft.player).exists(this.quest)) {
            minecraft.setScreen(new QuestsScreen());
        }
    }

    /**
     * Update the quest from the player's most recently synced set.
     * This prevents quest data from going stale between screen views.
     */
    protected void updateQuest() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            QuestsClient.getPlayerQuest(minecraft.player, quest.id()).ifPresent(q -> {
                this.quest = q;
                this.renderer.setQuest(q);
            });
        }
    }

    protected void abandon() {
        AbandonQuest.send(quest.id());
    }
}

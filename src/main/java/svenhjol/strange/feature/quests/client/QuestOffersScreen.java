package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import svenhjol.strange.feature.quests.QuestResources;
import svenhjol.strange.feature.quests.Quests;

import java.util.List;
import java.util.UUID;

public class QuestOffersScreen extends Screen {
    protected UUID villagerUuid;

    public QuestOffersScreen(UUID villagerUuid) {
        super(QuestResources.QUEST_OFFERS_TITLE);
        this.villagerUuid = villagerUuid;
    }

    @Override
    protected void init() {
        var midX = this.width / 2;
        var midY = this.height / 2;

        var quests = Quests.VILLAGER_QUESTS.getOrDefault(villagerUuid, List.of());
        for (int i = 0; i < quests.size(); i++) {
            var quest = quests.get(i);
            var button = new StartQuestButton(midX - (StartQuestButton.WIDTH / 2), 40 + (i * 20), b -> {
            });

            this.addRenderableWidget(button);
        }
    }

    static class StartQuestButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = QuestResources.START_QUEST_BUTTON_TEXT;
        public StartQuestButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }
}

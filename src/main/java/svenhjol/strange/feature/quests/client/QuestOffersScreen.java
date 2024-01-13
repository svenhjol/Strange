package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestResources;
import svenhjol.strange.feature.quests.Quests;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestOffersScreen extends Screen {
    protected UUID villagerUuid;
    protected List<BaseQuestRenderer<?>> renderers = new ArrayList<>();
    protected int midX;

    public QuestOffersScreen(UUID villagerUuid) {
        super(QuestResources.QUEST_OFFERS_TITLE);
        this.villagerUuid = villagerUuid;

        var quests = Quests.VILLAGER_QUESTS.getOrDefault(villagerUuid, List.of());
        for (Quest<?> quest : quests) {
            var renderer = quest.type().makeRenderer(quest);
            renderers.add(renderer);
        }
    }

    @Override
    protected void init() {
        midX = width / 2;
        var yOffset = 40;
        for (BaseQuestRenderer<?> renderer : renderers) {
            renderer.initOffer(this, yOffset);
            yOffset += renderer.getOfferHeight();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        var yOffset = 40;
        for (int i = 0; i < renderers.size(); i++) {
            var renderer = renderers.get(i);
            renderer.renderOffer(this, guiGraphics, yOffset, mouseX, mouseY);
            yOffset += renderer.getOfferHeight();
        }
    }

    static class AcceptQuestButton extends Button {
        static int WIDTH = 70;
        static int HEIGHT = 20;
        static Component TEXT = QuestResources.ACCEPT_QUEST_BUTTON_TEXT;
        public AcceptQuestButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }
}

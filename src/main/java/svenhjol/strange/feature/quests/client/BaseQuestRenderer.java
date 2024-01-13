package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public abstract class BaseQuestRenderer<Q> {
    Q quest;

    public BaseQuestRenderer() {
    }

    public void setQuest(Q quest) {
        this.quest = quest;
    }

    public int getOfferHeight() {
        return 80;
    }

    public void initOffer(Screen screen, int yOffset) {
        // extend me
    }

    public void renderOffer(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        // extend me
    }

    public Q quest() {
        return quest;
    }
}

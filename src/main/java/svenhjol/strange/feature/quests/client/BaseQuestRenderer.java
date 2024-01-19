package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import svenhjol.strange.feature.quests.Quest;

public abstract class BaseQuestRenderer<Q extends Quest<?>> {
    Q quest;

    public BaseQuestRenderer() {}

    public Q quest() {
        return quest;
    }

    public void setQuest(Q quest) {
        this.quest = quest;
    }

    public int getPagedOfferHeight() {
        return 74;
    }

    public int getPagedActiveHeight() {
        return 40;
    }

    public void initPagedOffer(Screen screen, int yOffset) {
        // no op
    }

    public void initPagedActive(Screen screen, int yOffset) {
        // no op
    }

    public void initSelectedActive(Screen screen) {
        // no op
    }

    public void renderPagedOffer(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        // no op
    }

    public void renderPagedActive(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        // no op
    }

    public void renderSelectedActive(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // no op
    }
}

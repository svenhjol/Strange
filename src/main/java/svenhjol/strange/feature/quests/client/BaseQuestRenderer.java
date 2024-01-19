package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import svenhjol.strange.feature.travel_journal.client.BaseTravelJournalScreen;

public abstract class BaseQuestRenderer<Q> {
    Q quest;

    public BaseQuestRenderer() {}

    public Q quest() {
        return quest;
    }

    public void setQuest(Q quest) {
        this.quest = quest;
    }

    public int getOfferedHeight() {
        return 74;
    }

    public int getPagedHeight() {
        return 74;
    }

    public void initOffered(Screen screen, int yOffset) {
        // no op
    }

    public void initPaged(BaseTravelJournalScreen screen, int yOffset) {
        // no op
    }

    public void renderOffered(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        // no op
    }

    public void renderPaged(BaseTravelJournalScreen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        // no op
    }
}

package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestList;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.client.BaseTravelJournalScreen;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.*;
import svenhjol.strange.feature.travel_journal.client.TravelJournalResources;
import svenhjol.strange.helper.GuiHelper;

import java.util.ArrayList;
import java.util.List;

public class QuestsScreen extends BaseTravelJournalScreen {
    int page;
    boolean renderedButtons = false;
    List<BaseQuestRenderer<?>> renderers = new ArrayList<>();

    public QuestsScreen() {
        this(1);
    }

    public QuestsScreen(int page) {
        super(TravelJournalResources.QUESTS_TITLE);
        this.page = page;
        PageTracker.Screen.QUESTS.set();

        var quests = getQuests().all();
        for (Quest quest : quests) {
            var renderer = quest.type().makeRenderer(quest);
            renderers.add(renderer);
        }
    }

    @Override
    protected void init() {
        super.init();

        for (BaseQuestRenderer<?> renderer : renderers) {
            renderer.setUpdateAction(b -> Minecraft.getInstance().setScreen(new QuestScreen(renderer.quest())));
            renderer.initPagedActive(this);
        }

        addRenderableWidget(new CloseButton(midX - (CloseButton.WIDTH / 2), 220, b -> onClose()));
        initShortcuts();

        renderedButtons = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderQuests(guiGraphics, mouseX, mouseY);
    }

    protected void renderQuests(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var rows = 3;
        var yOffset = 40;

        var pages = renderers.size() / rows;
        var index = (page - 1) * rows;

        if (renderers.isEmpty()) {
            renderNoQuests(guiGraphics);
        }

        for (var y = 0; y < rows; y++) {
            if (index >= renderers.size()) {
                continue;
            }

            var renderer = renderers.get(index);
            renderer.renderPagedActive(guiGraphics, yOffset, mouseX, mouseY);
            yOffset += renderer.getPagedActiveHeight();

            index++;
        }

        if (!renderedButtons) {
            if (page > 1) {
                addRenderableWidget(new PreviousPageButton(midX - 30, 185, b -> openQuests(page - 1)));
            }
            if (page < pages || index < renderers.size()) {
                addRenderableWidget(new NextPageButton(midX + 10, 185, b -> openQuests(page + 1)));
            }
            renderedButtons = true;
        }
    }

    protected QuestList getQuests() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return new QuestList();
        }

        return Quests.getPlayerQuests(minecraft.player);
    }

    protected void renderNoQuests(GuiGraphics guiGraphics) {
        GuiHelper.drawCenteredString(guiGraphics, font, TravelJournalResources.NO_QUESTS_TEXT, midX, 50, 0x2f2725, false);
    }
}

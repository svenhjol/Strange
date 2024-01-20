package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.quests.client.BaseQuestRenderer;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.*;
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

        var quests = getQuests();
        for (Quest<?> quest : quests) {
            var renderer = quest.type().makeRenderer(quest);
            renderers.add(renderer);
        }
    }

    @Override
    protected void init() {
        super.init();
        var yOffset = 40;

        for (BaseQuestRenderer<?> renderer : renderers) {
            renderer.initPagedActive(this,
                b -> Minecraft.getInstance().setScreen(new QuestScreen(renderer.quest())),
                yOffset);
            yOffset += renderer.getPagedActiveHeight();
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
        var perPage = rows;
        var yOffset = 40;

        var pages = renderers.size() / perPage;
        var index = (page - 1) * perPage;

        if (renderers.isEmpty()) {
            renderNoQuests(guiGraphics);
        }

        for (var y = 0; y < rows; y++) {
            if (index >= renderers.size()) {
                continue;
            }

            var renderer = renderers.get(index);
            renderer.renderPagedActive(this, guiGraphics, yOffset, mouseX, mouseY);
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

    protected List<Quest<?>> getQuests() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return List.of();
        }

        return Quests.getQuests(minecraft.player);
    }

    protected void renderNoQuests(GuiGraphics guiGraphics) {
        GuiHelper.drawCenteredString(guiGraphics, font, TravelJournalResources.NO_QUESTS_TEXT, midX, 50, 0x2f2725, false);
    }
}

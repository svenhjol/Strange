package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.runestones.RunestoneHelper;
import svenhjol.strange.feature.runestones.RunestonesClient;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.TravelJournal;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;

public class LearnedScreen extends BaseTravelJournalScreen {
    boolean renderedPaginationButtons = false;
    int page;

    public LearnedScreen() {
        this(1);
    }

    public LearnedScreen(int page) {
        super(TravelJournalResources.LEARNED_TITLE);
        this.page = page;
        PageTracker.Screen.LEARNED.set();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new CloseButton(midX - (CloseButton.WIDTH / 2),220, b -> onClose()));
        initShortcuts();

        renderedPaginationButtons = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderLocations(guiGraphics, mouseX, mouseY);
    }

    protected void renderLocations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var rows = 6;
        var columns = 2;
        var perPage = rows * columns;

        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        var learned = TravelJournal.getLearned(minecraft.player).orElse(null);
        if (learned == null) return;

        var locations = learned.getLocations();
        var pages = locations.size() / perPage;
        var index = (page - 1) * perPage;

        if (locations.isEmpty()) {
            renderNoLocations(guiGraphics);
        } else {
            for (var x = 0; x < columns; x++) {
                for (var y = 0; y < rows; y++) {
                    if (index >= locations.size()) {
                        continue;
                    }

                    var location = locations.get(index);
                    var name = TextHelper.translatable(RunestoneHelper.getLocaleKey(location));
                    var runes = TextHelper.literal(RunestoneHelper.getRunicName(location))
                        .withStyle(RunestonesClient.ILLAGER_GLYPHS_STYLE);

                    guiGraphics.drawString(font, name, midX - 110 + (x * 115), 40 + (y * 24), 0x272422, false);
                    guiGraphics.drawString(font, runes, midX - 110 + (x * 115), 50 + (y * 24), 0xbfb7b5, false);

                    index++;
                }
            }
        }

        if (!renderedPaginationButtons) {
            if (page > 1) {
                addRenderableWidget(new PreviousPageButton(midX - 30, 75, b -> openLearned(page - 1)));
            }
            if (page < pages || index < locations.size()) {
                addRenderableWidget(new NextPageButton(midX + 10, 75, b -> openLearned(page + 1)));
            }

            renderedPaginationButtons = true;
        }
    }

    protected void renderNoLocations(GuiGraphics guiGraphics) {
        drawCenteredString(guiGraphics, TravelJournalResources.NO_LEARNED_LOCATIONS_HEADING_TEXT, midX, 50, 0x2f2725, false);
        guiGraphics.drawWordWrap(font, TravelJournalResources.NO_LEARNED_LOCATIONS_BODY_TEXT, midX - 100, 63, 200, 0x8f8785);
    }
}

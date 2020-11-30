package svenhjol.strange.traveljournals.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import svenhjol.strange.traveljournals.JournalEntry;
import svenhjol.strange.traveljournals.TravelJournals;
import svenhjol.strange.traveljournals.TravelJournalsClient;

import java.util.List;

@SuppressWarnings("ConstantConditions")
public class TravelJournalScreen extends BaseScreen {
    public static final int PER_PAGE = 5;

    private List<JournalEntry> entries = TravelJournalsClient.entries;
    private String message = "";
    private int page;
    private int buttonSpacing = 18;
    private int titleSpacing = 11;
    private int rowHeight = 20;
    private int titleY = 15;
    private int textX = 2;
    private int textY = 10;
    private int buttonY = 4;

    public TravelJournalScreen() {
        super(I18n.translate("item.strange.travel_journal"));
    }

    @Override
    protected void refreshData() {
        super.refreshData();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TravelJournalsClient.closeIfNotHolding(this.client);

        if (!isClientValid())
            return;

        super.render(matrices, mouseX, mouseY, delta);

        PlayerEntity player = this.client.player;
        int mid = this.width / 2;
        int x = mid - 90;
        int y = 20;
        int p = page - 1;
        int rightEdge = mid + 94;
        int size = entries.size();

        // draw title
        centredString(matrices, textRenderer, I18n.translate("gui.strange.travel_journal.title", size), mid, titleY, TEXT_COLOR);
        y += titleSpacing;

        List<JournalEntry> sublist;
        if (size > PER_PAGE) {
            if (p * PER_PAGE >= size || p * PER_PAGE < 0) { // out of range, reset
                page = 1;
                p = 0;
            }
            int max = Math.min(p * PER_PAGE + PER_PAGE, size);
            sublist = entries.subList(p * PER_PAGE, max);
        } else {
            sublist = entries;
        }

        for (JournalEntry entry : sublist) {
            int buttonOffsetX = rightEdge - 18;
            boolean atEntryPosition = TravelJournalsClient.isPlayerAtEntryPosition(player, entry);

            // draw row
            String bold = atEntryPosition ? "§o" : "";
            this.textRenderer.draw(matrices, bold + entry.name, x + textX, y + textY, DyeColor.byId(entry.color).getSignColor()); // TODO: how to get color not signColor

            // update button
            this.addButton(new TexturedButtonWidget(buttonOffsetX, y + buttonY, 20, 18, 220, 0, 19, BUTTONS, r -> updateEntry(entry)));
            buttonOffsetX -= buttonSpacing;

            y += rowHeight;
        }

        if (size > PER_PAGE) {
            this.textRenderer.draw(matrices, I18n.translate("gui.strange.travel_journal.page", page), x + 100, 157, SUB_COLOR);
            if (page > 1) {
                this.addButton(new TexturedButtonWidget(x + 140, 152, 20, 18, 140, 0, 19, BUTTONS, r -> {
                    --page;
                    redraw();
                }));
            }
            if (page * PER_PAGE < size) {
                this.addButton(new TexturedButtonWidget(x + 160, 152, 20, 18, 120, 0, 19, BUTTONS, r -> {
                    ++page;
                    redraw();
                }));
            }
        }

        // used to display generic messages on the journal screen
        if (!message.isEmpty())
            drawCenteredString(matrices, textRenderer, message, mid, 143, WARN_COLOR);
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addButton(new ButtonWidget((width / 2) - 110, y, w, h, new TranslatableText("gui.strange.travel_journal.new_entry"), (button) -> this.addEntry()));
        this.addButton(new ButtonWidget((width / 2) + 10, y, w, h, new TranslatableText("gui.strange.travel_journal.close"), button -> this.onClose()));
    }

    private void addEntry() {
        if (entries.size() <= TravelJournals.MAX_ENTRIES) {
            TravelJournalsClient.sendServerPacket(TravelJournals.MSG_SERVER_ADD_ENTRY, null);
        } else {
            message = I18n.translate("gui.strange.travel_journal.journal_full");
        }
    }

    private void updateEntry(JournalEntry entry) {
        client.openScreen(new UpdateEntryScreen(entry));
    }
}

package svenhjol.strange.traveljournals.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.strange.base.helper.NetworkHelper;
import svenhjol.strange.totems.TotemOfWandering;
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
    private int titleTop = 15;
    private int rowHeight = 20;

    boolean hasTotem = false;
    boolean hasRenderedRuneButton = false;
    boolean hasRenderedScrollButton = false;
    boolean hasRenderedEntries = false;

    public TravelJournalScreen() {
        super(I18n.translate("item.strange.travel_journal"));
        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();
        hasRenderedRuneButton = false;
        hasRenderedScrollButton = false;
        hasRenderedEntries = false;

        hasTotem = client.player.inventory.contains(new ItemStack(TotemOfWandering.TOTEM_OF_WANDERING));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TravelJournalsClient.closeIfNotHolding(this.client);

        if (!isClientValid())
            return;

        super.render(matrices, mouseX, mouseY, delta);

        PlayerEntity player = this.client.player;
        int mid = this.width / 2;
        int left = mid - 90;
        int top = 20;
        int currentPage = page - 1;
        int rightEdge = mid + 94;
        int size = entries.size();

        // draw title
        centeredString(matrices, textRenderer, I18n.translate("gui.strange.travel_journal.title", size), mid, titleTop, TEXT_COLOR);
        top += titleSpacing;


        // handle pagination
        List<JournalEntry> sublist;
        if (size > PER_PAGE) {
            if (currentPage * PER_PAGE >= size || currentPage * PER_PAGE < 0) { // out of range, reset
                page = 1;
                currentPage = 0;
            }
            int max = Math.min(currentPage * PER_PAGE + PER_PAGE, size);
            sublist = entries.subList(currentPage * PER_PAGE, max);
        } else {
            sublist = entries;
        }

        for (JournalEntry entry : sublist) {
            int buttonOffsetX = rightEdge - 18;
            boolean atEntryPosition = TravelJournalsClient.isPlayerAtEntryPosition(player, entry);

            // draw row
            String bold = atEntryPosition ? "§o" : "";
            String name = entry.name.substring(0, Math.min(entry.name.length(), NAME_CUTOFF));
            this.textRenderer.draw(matrices, bold + name, left + 2, top + 10, DyeColor.byId(entry.color).getSignColor()); // TODO: how to get color not signColor

            // update button
            this.addButton(new TexturedButtonWidget(buttonOffsetX, top + 4, 20, 18, 220, 0, 19, BUTTONS, button -> updateEntry(entry)));
            buttonOffsetX -= buttonSpacing;

            // totem button
            if (hasTotem && DimensionHelper.isDimension(client.world, entry.dim)) {
                this.addButton(new TexturedButtonWidget(buttonOffsetX, top + 4, 20, 18, 200, 0, 19, BUTTONS, button -> useTotem(entry)));
            }

            top += rowHeight;
        }

        if (size > PER_PAGE) {
            this.textRenderer.draw(matrices, I18n.translate("gui.strange.travel_journal.page", page), left + 100, 157, SUB_COLOR);
            if (page > 1) {
                this.addButton(new TexturedButtonWidget(left + 140, 152, 20, 18, 140, 0, 19, BUTTONS, button -> {
                    --page;
                    init();
                }));
            }
            if (page * PER_PAGE < size) {
                this.addButton(new TexturedButtonWidget(left + 160, 152, 20, 18, 120, 0, 19, BUTTONS, button -> {
                    ++page;
                    init();
                }));
            }
        }


        // display generic messages on the journal screen
        if (!message.isEmpty())
            centeredString(matrices, textRenderer, message, mid, 143, WARN_COLOR);


        /*
         * --- Buttons on right of page ---
         */

        top = 13;

        // button to open rune page
        if (!hasRenderedRuneButton) {
            this.addButton(new TexturedButtonWidget(mid + rightButtonXOffset, top, 20, 18, 180, 0, 19, BUTTONS, button -> openRuneScreen()));
            hasRenderedRuneButton = true;
            top += rightButtonYOffset;
        }

        // button to open scroll page
        if (!hasRenderedScrollButton) {
            this.addButton(new TexturedButtonWidget(mid + rightButtonXOffset, top, 20, 18, 20, 0, 19, BUTTONS, button -> openScrollScreen()));
            hasRenderedScrollButton = true;
            top += rightButtonYOffset;
        }
    }

    @Override
    protected void redraw() {
        super.redraw();
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addButton(new ButtonWidget((width / 2) - 110, y, w, h, new TranslatableText("gui.strange.travel_journal.new_entry"), button -> this.addEntry()));
        this.addButton(new ButtonWidget((width / 2) + 10, y, w, h, new TranslatableText("gui.strange.travel_journal.close"), button -> this.onClose()));
    }

    private void addEntry() {
        if (entries.size() < TravelJournals.MAX_ENTRIES) {
            NetworkHelper.sendEmptyPacketToServer(TravelJournals.MSG_SERVER_ADD_ENTRY);
        } else {
            message = I18n.translate("gui.strange.travel_journal.journal_full");
        }
    }

    private void updateEntry(JournalEntry entry) {
        client.openScreen(new UpdateEntryScreen(entry));
    }

    private void openRuneScreen() {
        client.openScreen(new LearnedRunesScreen());
    }

    private void openScrollScreen() { client.openScreen(new ActiveScrollsScreen()); }

    private void useTotem(JournalEntry entry) {
        NetworkHelper.sendPacketToServer(TravelJournals.MSG_SERVER_USE_TOTEM, buffer -> buffer.writeCompoundTag(entry.toTag()));
        init();
    }
}

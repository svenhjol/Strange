package svenhjol.strange.module.travel_journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.helper.NetworkHelper;
import svenhjol.strange.module.travel_journals.TravelJournalEntry;
import svenhjol.strange.module.travel_journals.TravelJournals;
import svenhjol.strange.module.travel_journals.TravelJournalsClient;

import java.util.List;

@SuppressWarnings("ConstantConditions")
public class TravelJournalEntriesScreen extends TravelJournalBaseScreen {
    public static final int PER_PAGE = 5;

    private List<TravelJournalEntry> entries = TravelJournalsClient.entries;
    private String message = "";
    private int lastPage;
    private final int buttonSpacing = 18;
    private final int titleSpacing = 11;
    private final int entryRowHeight = 20;

    protected boolean hasTotem = false;
    protected boolean hasRenderedEntries = false;

    public TravelJournalEntriesScreen() {
        super(I18n.get("item.strange.travel_journal"));
        this.passEvents = false;
        this.lastPage = TravelJournalsClient.lastEntryPage;
    }

    @Override
    protected void init() {
        super.init();

        hasRenderedEntries = false;
//        hasTotem = PlayerHelper.getInventory(client.player).contains(new ItemStack(TotemOfWandering.TOTEM_OF_WANDERING)); // TODO: phase2
        hasTotem = false;
        previousPage = Page.ENTRIES;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        Player player = this.minecraft.player;
        int mid = this.width / 2;
        int left = mid - 90;
        int top = 20;
        int currentPage = lastPage - 1;
        int rightEdge = mid + 94;
        int size = entries.size();

        // draw title
        String title;
        if (size == 0) {
            title = "gui.strange.travel_journal.entries_none";
        } else if (size == 1) {
            title = "gui.strange.travel_journal.entries_single";
        } else {
            title = "gui.strange.travel_journal.entries_multiple";
        }

        centeredString(matrices, font, I18n.get(title, size), mid, titleTop, TEXT_COLOR);
        top += titleSpacing;

        // handle pagination
        List<TravelJournalEntry> sublist;
        if (size > PER_PAGE) {
            if (currentPage * PER_PAGE >= size || currentPage * PER_PAGE < 0) { // out of range, reset
                lastPage = 1;
                currentPage = 0;
            }
            int max = Math.min(currentPage * PER_PAGE + PER_PAGE, size);
            sublist = entries.subList(currentPage * PER_PAGE, max);
        } else {
            sublist = entries;
        }

        for (TravelJournalEntry entry : sublist) {
            int buttonOffsetX = rightEdge - 18;
            boolean atEntryPosition = TravelJournalsClient.isPlayerAtEntryPosition(player, entry);

            // draw row
            String bold = atEntryPosition ? "§o" : "";
            String name = entry.name.substring(0, Math.min(entry.name.length(), NAME_CUTOFF));
            this.font.draw(matrices, bold + name, left + 2, top + 10, DyeColor.byId(entry.color).getTextColor()); // TODO: how to get color not signColor

            // update button
            this.addRenderableWidget(new ImageButton(buttonOffsetX, top + 4, 20, 18, 180, 0, 19, BUTTONS, button -> updateEntry(entry)));
            buttonOffsetX -= buttonSpacing;

            // totem button
            if (hasTotem && DimensionHelper.isDimension(minecraft.level, entry.dim)) {
                this.addRenderableWidget(new ImageButton(buttonOffsetX, top + 4, 20, 18, 160, 0, 19, BUTTONS, button -> useTotem(entry)));
            }

            top += entryRowHeight;
        }

        if (size > PER_PAGE) {
            this.font.draw(matrices, I18n.get("gui.strange.travel_journal.page", lastPage), left + 100, 157, SUB_COLOR);
            if (lastPage > 1) {
                this.addRenderableWidget(new ImageButton(left + 140, 152, 20, 18, 140, 0, 19, BUTTONS, button -> {
                    --lastPage;
                    init();
                }));
            }
            if (lastPage * PER_PAGE < size) {
                this.addRenderableWidget(new ImageButton(left + 160, 152, 20, 18, 120, 0, 19, BUTTONS, button -> {
                    ++lastPage;
                    init();
                }));
            }
        }

        // display generic messages on the journal screen
        if (!message.isEmpty())
            centeredString(matrices, font, message, mid, 143, WARN_COLOR);

        // remember the page of this journal
        TravelJournalsClient.lastEntryPage = lastPage;
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

        this.addRenderableWidget(new Button((width / 2) - 110, y, w, h, new TranslatableComponent("gui.strange.travel_journal.new_entry"), button -> this.addEntry()));
        this.addRenderableWidget(new Button((width / 2) + 10, y, w, h, new TranslatableComponent("gui.strange.travel_journal.close"), button -> this.onClose()));
    }

    private void addEntry() {
        if (entries.size() < TravelJournals.MAX_ENTRIES) {
            NetworkHelper.sendEmptyPacketToServer(TravelJournals.MSG_SERVER_ADD_ENTRY);
        } else {
            message = I18n.get("gui.strange.travel_journal.journal_full");
        }
    }

    private void updateEntry(TravelJournalEntry entry) {
        minecraft.setScreen(new TravelJournalUpdateEntryScreen(entry));
    }

    private void useTotem(TravelJournalEntry entry) {
        NetworkHelper.sendPacketToServer(TravelJournals.MSG_SERVER_USE_TOTEM, buffer -> buffer.writeNbt(entry.toNbt()));
        minecraft.setScreen(null);
    }
}

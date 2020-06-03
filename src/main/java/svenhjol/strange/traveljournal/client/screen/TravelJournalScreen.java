package svenhjol.strange.traveljournal.client.screen;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.meson.Meson;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.item.TravelJournalItem;
import svenhjol.strange.traveljournal.message.ServerTravelJournalAction;
import svenhjol.strange.traveljournal.message.ServerTravelJournalMeta;
import svenhjol.strange.traveljournal.module.TravelJournal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TravelJournalScreen extends BaseTravelJournalScreen {
    public static final int PER_PAGE = 5;

    protected Map<String, Entry> sortedEntries = new TreeMap<>();
    protected List<String> ids = new ArrayList<>();
    protected final CompoundNBT journalEntries;
    protected int page;
    protected String message = "";

    public TravelJournalScreen(PlayerEntity player, Hand hand) {
        super(I18n.format("item.strange.travel_journal"), player, hand);
        this.page = TravelJournalItem.getPage(stack);
        this.journalEntries = TravelJournalItem.getEntries(stack);
    }

    @Override
    protected void refreshData() {
        this.ids = new ArrayList<>();
        this.sortedEntries = new TreeMap<>();

        for (String id : journalEntries.keySet()) {
            INBT entryTag = journalEntries.get(id);
            if (entryTag == null) continue;
            Entry entry = new Entry((CompoundNBT) entryTag);

            this.ids.add(id);
            this.sortedEntries.put(id, entry);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        TravelJournal.client.closeIfNotHolding(mc, player, hand);
        renderBackground();
        renderBackgroundTexture();

        int mid = this.width / 2;
        int x = mid - 90;
        int y = 20;
        int p = page - 1;
        int buttonSpacing = 18;
        int titleSpacing = 11;
        int rowHeight = 20;
        int titleY = 15;
        int textX = 2;
        int textY = 10;
        int buttonY = 4;
        int rightEdge = mid + 94;

        drawCenteredString(this.font, I18n.format("gui.strange.travel_journal.title", ids.size()), mid, titleY, TEXT_COLOR);
        y += titleSpacing;

        List<String> sublist;
        int size = ids.size();
        if (ids.size() > PER_PAGE) {
            if (p * PER_PAGE >= size || p * PER_PAGE < 0) { // out of range, reset
                page = 1;
                p = 0;
            }
            int max = Math.min(p * PER_PAGE + PER_PAGE, size);
            sublist = ids.subList(p * PER_PAGE, max);
        } else {
            sublist = ids;
        }

        for (String id : sublist) {
            int buttonOffsetX = rightEdge - 18;
            Entry entry = sortedEntries.get(id);
            boolean atEntryPosition = TravelJournal.client.isPlayerAtEntryPosition(player, entry);

            // draw row
            String bold = atEntryPosition ? "§o" : "";
            this.font.drawString(bold + entry.name, x + textX, y + textY, DyeColor.byId(entry.color).getColorValue());

            // update button
            this.addButton(new ImageButton(buttonOffsetX, y + buttonY, 20, 18, 220, 0, 19, BUTTONS, (r) -> update(entry)));
            buttonOffsetX -= buttonSpacing;

            y += rowHeight;
        }

        if (ids.size() > PER_PAGE) {
            this.font.drawString(I18n.format("gui.strange.travel_journal.page", page), x + 100, 157, SUB_COLOR);
            if (page > 1) {
                this.addButton(new ImageButton(x + 140, 152, 20, 18, 140, 0, 19, BUTTONS, (r) -> {
                    updateServerPage(--page);
                    redraw();
                }));
            }
            if (page * PER_PAGE < ids.size()) {
                this.addButton(new ImageButton(x + 160, 152, 20, 18, 120, 0, 19, BUTTONS, (r) -> {
                    updateServerPage(++page);
                    redraw();
                }));
            }
        }

        if (!this.message.isEmpty())
            this.drawCenteredString(this.font, this.message, mid, 143, WARN_COLOR);

        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addButton(new Button((width / 2) - 110, y, w, h, I18n.format("gui.strange.travel_journal.new_entry"), (button) -> this.add()));
        this.addButton(new Button((width / 2) + 10, y, w, h, I18n.format("gui.strange.travel_journal.close"), (button) -> this.close()));
    }

    private void add() {
        ItemStack held = player.getHeldItem(hand);
        CompoundNBT entries = TravelJournalItem.getEntries(held);
        if (entries.keySet().size() >= TravelJournal.maxEntries) {
            this.message = I18n.format("gui.strange.travel_journal.journal_full");
            return;
        }

        BlockPos playerPos = player.getPosition();
        Biome biome = player.world.getBiome(playerPos);
        String biomeName = biome.getDisplayName().getUnformattedComponentText();

        Entry entry = new Entry(
            Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(8),
            I18n.format("gui.strange.travel_journal.new_biome_entry", biomeName),
            player.getPosition(),
            WorldHelper.getDimensionId(player.world),
            15
        );

        TravelJournalItem.addEntry(held, entry);
        Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToServer(new ServerTravelJournalAction(ServerTravelJournalAction.ADD, entry, hand));
        mc.displayGuiScreen(new UpdateEntryScreen(entry, player, hand));
    }

    private void update(Entry entry) {
        mc.displayGuiScreen(new UpdateEntryScreen(entry, player, hand));
    }

    private void updateServerPage(int page) {
        Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToServer(new ServerTravelJournalMeta(ServerTravelJournalMeta.SETPAGE, hand, page));
    }
}

package svenhjol.strange.traveljournal.client.screen;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.totems.module.TotemOfReturning;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.item.TravelJournalItem;
import svenhjol.strange.traveljournal.message.ActionMessage;
import svenhjol.strange.traveljournal.message.ClientEntriesMessage;
import svenhjol.strange.traveljournal.message.MetaMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TravelJournalScreen extends BaseTravelJournalScreen
{
    public static int PER_PAGE = 3;

    protected Map<String, Entry> sortedEntries = new TreeMap<>();
    protected List<String> ids = new ArrayList<>();
    protected List<String> hasScreenshots = new ArrayList<>();
    protected CompoundNBT journalEntries = new CompoundNBT();
    protected boolean hasTotem = false;
    protected int page;

    public TravelJournalScreen(PlayerEntity player, Hand hand)
    {
        super(I18n.format("item.strange.travel_journal"), player, hand);
        this.page = TravelJournalItem.getPage(stack);
        this.journalEntries = TravelJournalItem.getEntries(stack);

        startUpdateCheck();
    }

    @Override
    protected void refreshData()
    {
        this.ids = new ArrayList<>();
        this.sortedEntries = new TreeMap<>();
        this.hasScreenshots = new ArrayList<>();

        for (String id : journalEntries.keySet()) {
            INBT entryTag = journalEntries.get(id);
            if (entryTag == null) continue;
            Entry entry = new Entry((CompoundNBT)entryTag);

            this.ids.add(id);
            this.sortedEntries.put(id, entry);

            File file = ScreenshotScreen.getScreenshot(entry);
            if (file.exists()) hasScreenshots.add(id);
        }

        // check if player has a totem in their inventory
        PlayerInventory inventory = player.inventory;
        ImmutableList<NonNullList<ItemStack>> inventories = ImmutableList.of(inventory.mainInventory, inventory.offHandInventory);

        for (NonNullList<ItemStack> itemStacks : inventories) {
            for (ItemStack stack : itemStacks) {
                if (!stack.isEmpty() && stack.getItem() == TotemOfReturning.item) {
                    hasTotem = true;
                    break;
                }
            }
            if (hasTotem) break;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground();
        renderBackgroundTexture();
        checkServerUpdates(mc -> journalEntries = ClientEntriesMessage.Handler.entries);

        int mid = this.width / 2;
        int x = mid - 80;
        int y = 20;
        int p = page - 1;
        int buttonSpacing = 21;
        int titleSpacing = 6;
        int rowHeight = 39;
        int titleX = 2;
        int titleY = 15;
        int textX = 2;
        int textY = 10;
        int buttonX = 2;
        int buttonY = 20;

        this.font.drawString(I18n.format("gui.strange.travel_journal.title", ids.size()), x + titleX, titleY, TEXT_COLOR);
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
            int buttonOffsetX = buttonX;
            Entry entry = sortedEntries.get(id);

            boolean hasScreenshot = hasScreenshots.contains(id);
            boolean atPosition = entry.pos != null && WorldHelper.getDistanceSq(player.getPosition(), entry.pos) < TravelJournalItem.SCREENSHOT_DISTANCE;

            // draw row
            this.font.drawString(entry.name, x + textX, y + textY, entry.color);

            // update button
            this.addButton(new ImageButton(x + buttonOffsetX, y + buttonY, 20, 18, 40, 0, 19, BUTTONS, (r) -> update(entry)));
            buttonOffsetX += buttonSpacing;

            // delete button
            this.addButton(new ImageButton(x + buttonOffsetX, y + buttonY, 20, 18, 60, 0, 19, BUTTONS, (r) -> delete(entry)));
            buttonOffsetX += buttonSpacing;

            // screenshot button
            if (hasScreenshot || atPosition) {
                this.addButton(new ImageButton(x + buttonOffsetX, y + buttonY, 20, 18, atPosition ? 100 : 80, 0, 19, BUTTONS, (r) -> screenshot(entry)));
                buttonOffsetX += buttonSpacing;
            }

            // teleport button
            if (hasTotem && entry.pos != null) {
                this.addButton(new ImageButton(x + buttonOffsetX, y + buttonY, 20, 18, 20, 0, 19, BUTTONS, (r) -> teleport(entry)));
                buttonOffsetX += buttonSpacing;
            }

            y += rowHeight;
        }

        if (ids.size() > PER_PAGE) {
            this.font.drawString(I18n.format("gui.strange.travel_journal.page", page), x + 90, 157, SUB_COLOR);
            if (page > 1) {
                this.addButton(new ImageButton(x + 130, 152, 20, 18, 140, 0, 19, BUTTONS, (r) -> {
                    updateServerPage(--page);
                    redraw();
                }));
            }
            if (page * PER_PAGE < ids.size()) {
                this.addButton(new ImageButton(x + 150, 152, 20, 18, 120, 0, 19, BUTTONS, (r) -> {
                    updateServerPage(++page);
                    redraw();
                }));
            }
        }

        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderButtons()
    {
        int y = (height / 4) + 160;
        int w = 100;
        int h = 20;

        this.addButton(new Button((width / 2) - 110, y, w, h, I18n.format("gui.strange.travel_journal.new_entry"), (button) -> this.add()));
        this.addButton(new Button((width / 2) + 10, y, w, h, I18n.format("gui.strange.travel_journal.close"), (button) -> this.close()));
    }

    private void add()
    {
        Entry entry = new Entry(
            Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(8),
            player.getPosition(),
            WorldHelper.getDimensionId(player.world)
        );

        PacketHandler.sendToServer(new ActionMessage(ActionMessage.ADD, entry, hand));
        startUpdateCheck();
    }

    private void update(Entry entry)
    {
        mc.displayGuiScreen(new UpdateEntryScreen(entry, player, hand));
    }

    private void delete(Entry entry)
    {
        startUpdateCheck();
        PacketHandler.sendToServer(new ActionMessage(ActionMessage.DELETE, entry, hand));
    }

    private void teleport(Entry entry)
    {
        this.close();
        PacketHandler.sendToServer(new ActionMessage(ActionMessage.TELEPORT, entry, hand));
    }

    private void screenshot(Entry entry)
    {
        mc.displayGuiScreen(new ScreenshotScreen(entry, player, hand));
    }

    private void updateServerPage(int page)
    {
        PacketHandler.sendToServer(new MetaMessage(MetaMessage.SETPAGE, hand, page));
    }
}

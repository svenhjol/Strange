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
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.totems.module.TotemOfReturning;
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
    protected Map<String, CompoundNBT> sortedEntries = new TreeMap<>();
    protected List<String> ids = new ArrayList<>();
    protected List<String> hasScreenshots = new ArrayList<>();
    protected CompoundNBT journalEntries = new CompoundNBT();
    protected boolean hasTotem = false;
    protected long checkForUpdates = 0;
    protected int page = 1;
    protected CompoundNBT updating = null;

    public TravelJournalScreen(PlayerEntity player, Hand hand)
    {
        super(I18n.format("item.strange.travel_journal"), player, hand);

        this.page = TravelJournalItem.getPage(stack);
        this.journalEntries = TravelJournalItem.getEntries(stack);

        startUpdateCheck();
    }

    @Override
    protected void init()
    {
        renderButtons();
        refreshData();

        //this.minecraft.keyboardListener.enableRepeatEvents(true);
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
            this.ids.add(id);
            this.sortedEntries.put(id, (CompoundNBT)entryTag);

            File file = ScreenshotScreen.getScreenshot(id);
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
        this.renderBackground();
        this.renderBackgroundTexture();
        this.checkServerUpdates();

        int mid = this.width / 2;
        int fontColor = 0x000000;
        int p = page - 1;
        int perPage = 3;
        int x = mid - 80;
        int y = 20;
        int buttonSpacing = 21;
        int titleSpacing = 6;
        int rowHeight = 39;
        int titleX = 2;
        int titleY = 15;
        int textX = 2;
        int textY = 10;
        int buttonX = 2;
        int buttonY = 20;

        this.font.drawString(I18n.format("gui.strange.travel_journal.title", ids.size()), x + titleX, titleY, fontColor);
        y += titleSpacing; // spacing

        List<String> sublist;
        int size = ids.size();
        if (ids.size() > perPage) {
            if (p * perPage >= size || p * perPage < 0) { // the actual fuck
                page = 1;
                p = 0;
            }
            int max = Math.min(p * perPage + perPage, size);
            sublist = ids.subList(p * perPage, max);
        } else {
            sublist = ids;
        }


        for (String id : sublist) {
            int buttonOffsetX = buttonX;
            CompoundNBT entry = sortedEntries.get(id);

            String name = entry.getString(TravelJournalItem.NAME);
            long posLong = entry.getLong(TravelJournalItem.POS);
            final BlockPos pos = posLong != 0 ? BlockPos.fromLong(posLong) : null;
            int color = entry.getInt(TravelJournalItem.COLOR);

            boolean hasScreenshot = hasScreenshots.contains(id);
            boolean atPosition = pos != null && WorldHelper.getDistanceSq(player.getPosition(), pos) < 10;

            // draw row
            this.font.drawString(name, x + textX, y + textY, color);

            // update button
            this.addButton(new ImageButton(x + buttonOffsetX, y + buttonY, 20, 18, 40, 0, 19, BUTTONS, (r) -> {
                update(id, entry);
            }));
            buttonOffsetX += buttonSpacing;

            // delete button
            this.addButton(new ImageButton(x + buttonOffsetX, y + buttonY, 20, 18, 60, 0, 19, BUTTONS, (r) -> delete(id)));
            buttonOffsetX += buttonSpacing;

            // screenshot button
            if (hasScreenshot || atPosition) {
                this.addButton(new ImageButton(x + buttonOffsetX, y + buttonY, 20, 18, atPosition ? 100 : 80, 0, 19, BUTTONS, (r) -> {
//                    if (!hasScreenshot && atPosition) takeScreenshot(id, name);
                    showScreenshot(id, name, pos);
                }));
                buttonOffsetX += buttonSpacing;
            }

            // teleport button
            if (hasTotem && pos != null) {
                this.addButton(new ImageButton(x + buttonOffsetX, y + buttonY, 20, 18, 20, 0, 19, BUTTONS, (r) -> teleport(id)));
                buttonOffsetX += buttonSpacing;
            }

            y += rowHeight;
        }

        if (ids.size() > perPage) {
            this.font.drawString(I18n.format("gui.strange.travel_journal.page", page), x + 90, 157, 0xB4B0A8);
            if (page > 1) {
                this.addButton(new ImageButton(x + 130, 152, 20, 18, 140, 0, 19, BUTTONS, (r) -> {
                    updatePageOnServer(--page);
                    redraw();
                }));
            }
            if (page * perPage < ids.size()) {
                this.addButton(new ImageButton(x + 150, 152, 20, 18, 120, 0, 19, BUTTONS, (r) -> {
                    updatePageOnServer(++page);
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
//        NativeImage screenshot = ScreenShotHelper.createScreenshot(win.getFramebufferWidth() / 6, win.getFramebufferHeight() / 6, mc.getFramebuffer());
//        int[] arr = screenshot.makePixelArray();
//
//
//        ByteArrayOutputStream bs = new ByteArrayOutputStream();
//        ObjectOutputStream out = new ObjectOutputStream(bs);
//        out.writeObject(arr);
//        out.flush();
//        out.close();
//
//        byte[] bytes = bs.toByteArray();
//
//        ByteArrayOutputStream bs2 = new ByteArrayOutputStream();
//        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION, true);
//        DeflaterOutputStream ds = new DeflaterOutputStream(bs2, compressor);
//        ds.write(bytes);
//        ds.close();
        String id = Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(8);
        PacketHandler.sendToServer(new ActionMessage(ActionMessage.ADD, id, hand, 0, WorldHelper.getDimensionId(player.world), player.getPosition(), null));
        startUpdateCheck();
    }

    private void update(String id, CompoundNBT entry)
    {
        this.close();
        mc.displayGuiScreen(new UpdateEntryScreen(id, entry, player, hand));
    }

    private void delete(String id)
    {
        startUpdateCheck();
        PacketHandler.sendToServer(new ActionMessage(ActionMessage.DELETE, id, hand));
    }

    private void teleport(String id)
    {
        this.close();
        PacketHandler.sendToServer(new ActionMessage(ActionMessage.TELEPORT, id, hand));
    }

    private void showScreenshot(String id, String title, BlockPos pos)
    {
        this.close();
        mc.displayGuiScreen(new ScreenshotScreen(id, title, pos, player, hand));
    }

    private void takeScreenshot(String id, String title)
    {
        this.close();
        ScreenshotScreen.takeScreenshot(id, hand);
    }

    private void updatePageOnServer(int page)
    {
        PacketHandler.sendToServer(new MetaMessage(MetaMessage.SETPAGE, hand, page));
    }

    private void startUpdateCheck()
    {
        checkForUpdates = mc.world.getGameTime();
    }

    private void checkServerUpdates()
    {
        if (checkForUpdates > 0) {
            if (ClientEntriesMessage.Handler.updated) {
                journalEntries = ClientEntriesMessage.Handler.entries;
                ClientEntriesMessage.Handler.clearUpdates();
                checkForUpdates = 0;
                refreshData();
                redraw();
            } else if (mc.world.getGameTime() - checkForUpdates > 60) {
                checkForUpdates = 0;
                refreshData();
                redraw();
            }
        }
    }
}

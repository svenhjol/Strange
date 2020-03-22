package svenhjol.strange.traveljournal.client;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.base.helper.VersionHelper;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.client.screen.BaseTravelJournalScreen;
import svenhjol.strange.traveljournal.client.screen.TravelJournalScreen;
import svenhjol.strange.traveljournal.client.screen.UpdateEntryScreen;
import svenhjol.strange.traveljournal.item.TravelJournalItem;

@OnlyIn(Dist.CLIENT)
public class TravelJournalClient {
    public boolean updateAfterScreenshot = false;

    public void openTravelJournal(Hand hand) {
        PlayerEntity player = ClientHelper.getClientPlayer();
        player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        Minecraft.getInstance().displayGuiScreen(new TravelJournalScreen(player, hand));
    }

    public void handleAddEntry(Entry entry, Hand hand) {
        PlayerEntity player = ClientHelper.getClientPlayer();
        player.playSound(SoundEvents.ITEM_BOOK_PUT, 1.0F, 1.0F);
        Minecraft.getInstance().displayGuiScreen(new UpdateEntryScreen(entry, player, hand));
    }

    public void handleScreenshot(Entry entry, Hand hand) {
        PlayerEntity player = ClientHelper.getClientPlayer();
        Minecraft mc = Minecraft.getInstance();
        mc.gameSettings.hideGUI = true;

        MainWindow win = VersionHelper.getMainWindow(mc);
        ScreenShotHelper.saveScreenshot(mc.gameDir, entry.id + ".png", win.getFramebufferWidth() / 8, win.getFramebufferHeight() / 8, mc.getFramebuffer(), i -> mc.gameSettings.hideGUI = false);
        player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
        updateAfterScreenshot = true;

        try {
            Thread.sleep(500);
            returnAfterScreenshot(mc, entry, player, hand);
        } catch (InterruptedException e) {
            // don't try and reopen the journal
            Strange.LOG.debug("Thread sleep failed: " + e.getMessage());
        }
    }

    public void returnAfterScreenshot(Minecraft mc, Entry entry, PlayerEntity player, Hand hand) {
        BaseTravelJournalScreen screen = updateAfterScreenshot ? new UpdateEntryScreen(entry, player, hand) : new TravelJournalScreen(player, hand);
        updateAfterScreenshot = false;
        mc.displayGuiScreen(screen);
    }

    public boolean isPlayerAtEntryPosition(PlayerEntity player, Entry entry) {
        return entry.pos != null && WorldHelper.getDistanceSq(player.getPosition(), entry.pos) < TravelJournalItem.SCREENSHOT_DISTANCE;
    }

    public void closeIfNotHolding(Minecraft mc, PlayerEntity player, Hand hand) {
        if (!(player.getHeldItem(hand).getItem() instanceof TravelJournalItem)) {
            mc.displayGuiScreen(null);
        }
    }
}


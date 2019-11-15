package svenhjol.strange.traveljournal.client;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.client.screen.BaseTravelJournalScreen;
import svenhjol.strange.traveljournal.client.screen.ScreenshotScreen;
import svenhjol.strange.traveljournal.client.screen.TravelJournalScreen;
import svenhjol.strange.traveljournal.client.screen.UpdateEntryScreen;
import svenhjol.strange.traveljournal.item.TravelJournalItem;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class TravelJournalClient
{
    public boolean updateAfterScreenshot = false;

    public void openTravelJournal(Hand hand)
    {
        PlayerEntity player = ClientHelper.getClientPlayer();
        Minecraft.getInstance().displayGuiScreen(new TravelJournalScreen(player, hand));
    }

    public void takeScreenshot(String id, Consumer<ITextComponent> onFinish)
    {
        Minecraft mc = Minecraft.getInstance();
        MainWindow win = mc.mainWindow;
        ScreenShotHelper.saveScreenshot(mc.gameDir, id, win.getFramebufferWidth() / 8, win.getFramebufferHeight() / 8, mc.getFramebuffer(), onFinish);
    }

    public void handleAddEntry(Entry entry, Hand hand)
    {
        PlayerEntity player = ClientHelper.getClientPlayer();
        Minecraft.getInstance().displayGuiScreen(new UpdateEntryScreen(entry, player, hand));
    }

    public void handleScreenshot(Entry entry, Hand hand)
    {
        PlayerEntity player = ClientHelper.getClientPlayer();
        takeScreenshot(entry.id, result -> {
            player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
            Minecraft.getInstance().displayGuiScreen(new ScreenshotScreen(entry, player, hand));
        });
    }

    public void returnAfterScreenshot(Minecraft mc, Entry entry, PlayerEntity player, Hand hand)
    {
        BaseTravelJournalScreen screen = updateAfterScreenshot ? new UpdateEntryScreen(entry, player, hand) : new TravelJournalScreen(player, hand);
        updateAfterScreenshot = false;
        mc.displayGuiScreen(screen);
    }

    public boolean isPlayerAtEntryPosition(PlayerEntity player, Entry entry)
    {
        return entry.pos != null && WorldHelper.getDistanceSq(player.getPosition(), entry.pos) < TravelJournalItem.SCREENSHOT_DISTANCE;
    }
}


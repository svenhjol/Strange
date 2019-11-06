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
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.client.screen.ScreenshotScreen;
import svenhjol.strange.traveljournal.client.screen.TravelJournalScreen;
import svenhjol.strange.traveljournal.client.screen.UpdateEntryScreen;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class TravelJournalClient
{
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

    public void handleAddEntry(Hand hand, Entry entry)
    {
        PlayerEntity player = ClientHelper.getClientPlayer();
        Minecraft.getInstance().displayGuiScreen(new UpdateEntryScreen(entry, player, hand));
    }

    public void handleScreenshot(Hand hand, Entry entry)
    {
        PlayerEntity player = ClientHelper.getClientPlayer();
        takeScreenshot(entry.id, result -> {
            player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
            Minecraft.getInstance().displayGuiScreen(new ScreenshotScreen(entry, player, hand));
        });
    }
}

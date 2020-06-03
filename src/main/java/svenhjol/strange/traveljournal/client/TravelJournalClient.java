package svenhjol.strange.traveljournal.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
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
import svenhjol.strange.traveljournal.item.TravelJournalPage;

@OnlyIn(Dist.CLIENT)
public class TravelJournalClient {
    public boolean updateAfterScreenshot = false;
    public PageItemRenderer pageItemRenderer;

    public TravelJournalClient() {
        this.pageItemRenderer = new PageItemRenderer(
            Minecraft.getInstance().textureManager
        );
    }

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

    public void renderPageHand(MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, Hand hand, float pitch, float equip, float swing, ItemStack stack) {
        Entry entry = TravelJournalPage.getEntry(stack);
        int display = TravelJournalPage.getDisplay(stack);
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;

        matrixStack.push(); // needed so that parent renderer isn't affect by what we do here

        // copypasta from renderMapFirstPersonSide
        float e = hand == Hand.MAIN_HAND ? 1.0F : -1.0F;
        matrixStack.translate((double)(e * 0.125F), -0.125D, 0.0D);

        // render player arm
        if (!player.isInvisible()) {
            matrixStack.push();
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(e * 10.0F));
            pageItemRenderer.renderArm(player, matrixStack, buffers, light, swing, equip, hand);
            matrixStack.pop();
        }

        // transform page based on the hand it is held and render it
        matrixStack.push();
        pageItemRenderer.transformPageForHand(matrixStack, buffers, light, swing, equip, hand);
        if (display == 0) {
            pageItemRenderer.renderEntryPage(entry, matrixStack, buffers, light);
        }
        if (display == 1) {
            pageItemRenderer.renderRunePage(entry, matrixStack, buffers, light);
        }
        matrixStack.pop();

        matrixStack.pop(); // close
    }


    /**
     * Return the angle to render the Map
     */
    private float getMapAngleFromPitch(float pitch) {
        float f = 1.0F - pitch / 45.0F + 0.1F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = -MathHelper.cos(f * (float)Math.PI) * 0.5F + 0.5F;
        return f;
    }
}


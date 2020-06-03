package svenhjol.strange.traveljournal.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
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
    public RunePageRenderer runePageRenderer;
    public final RenderType PAGE_BACKGROUND = RenderType.getText(new ResourceLocation(Strange.MOD_ID, "textures/gui/rune_page_background.png"));


    public TravelJournalClient() {
        this.runePageRenderer = new RunePageRenderer(
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
        matrixStack.push();

        float f = MathHelper.sqrt(swing);
        float f1 = -0.2F * MathHelper.sin(swing * (float)Math.PI);
        float f2 = -0.4F * MathHelper.sin(f * (float)Math.PI);
        matrixStack.translate(0.0D, (double)(-f1 / 2.0F), (double)f2);
        float f3 = this.getMapAngleFromPitch(pitch);
        matrixStack.translate(0.0D, (double)(0.04F + equip
            * -1.2F + f3 * -0.5F), (double)-0.72F);
        matrixStack.rotate(Vector3f.XP.rotationDegrees(f3 * -85.0F));

        float f4 = MathHelper.sin(f * (float)Math.PI);
        matrixStack.rotate(Vector3f.XP.rotationDegrees(f4 * 20.0F));
        matrixStack.scale(2.0F, 2.0F, 2.0F);

        matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F));
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(180.0F));
        matrixStack.scale(0.38F, 0.38F, 0.38F);
        matrixStack.translate(-0.5D, -0.5D, 0.0D);
        matrixStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
        IVertexBuilder builder = buffers.getBuffer(PAGE_BACKGROUND);
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        builder.pos(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).tex(0.0F, 1.0F).lightmap(light).endVertex();
        builder.pos(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).tex(1.0F, 1.0F).lightmap(light).endVertex();
        builder.pos(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).tex(1.0F, 0.0F).lightmap(light).endVertex();
        builder.pos(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).tex(0.0F, 0.0F).lightmap(light).endVertex();

        Entry entry = TravelJournalPage.getEntry(stack);
        runePageRenderer.render(entry, matrixStack, buffers, light);

        matrixStack.pop();
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


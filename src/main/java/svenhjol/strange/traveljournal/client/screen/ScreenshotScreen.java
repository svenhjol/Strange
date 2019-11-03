package svenhjol.strange.traveljournal.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import svenhjol.meson.Meson;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.item.TravelJournalItem;
import svenhjol.strange.traveljournal.message.ServerTravelJournalAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ScreenshotScreen extends BaseTravelJournalScreen
{
    protected Entry entry;
    protected File file = null;
    protected DynamicTexture tex = null;
    protected ResourceLocation res = null;

    public ScreenshotScreen(Entry entry, PlayerEntity player, Hand hand)
    {
        super(entry.name, player, hand);
        this.entry = entry;
    }

    @Override
    protected void init()
    {
        super.init();
        if (!mc.world.isRemote) return;
        file = getScreenshot(entry);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        if (hasScreenshot()) {
            this.renderBackground();

            int mid = this.width / 2;
            this.drawCenteredString(this.font, entry.name, mid, 10, 0xFFFFFF);

            if (tex == null) {
                try {
                    InputStream stream = new FileInputStream(file);
                    NativeImage screenshot = NativeImage.read(stream);
                    tex = new DynamicTexture(screenshot);
                    res = this.mc.getTextureManager().getDynamicTextureLocation("screenshot", tex);
                    stream.close();

                    if (tex == null || res == null) {
                        Meson.warn("Failed to load screenshot");
                        this.close();
                    }

                } catch (Exception e) {
                    Meson.warn("Error loading screenshot", e);
                    this.close();
                }
            }

            if (res != null) {
                mc.textureManager.bindTexture(res);
                GlStateManager.pushMatrix();
                GlStateManager.scalef(1.0F, 0.66F, 1.0F);
                this.blit(((this.width - 256) / 2) + 13, 36, 0, 0, 230, 200);
                GlStateManager.popMatrix();
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
        int buttonOffsetX = -50;

        if (WorldHelper.getDistanceSq(player.getPosition(), entry.pos) < TravelJournalItem.SCREENSHOT_DISTANCE) {
            this.addButton(new Button((width / 2) - 110, y, w, h, I18n.format("gui.strange.travel_journal.new_screenshot"), (button) -> {
                this.close();
                takeScreenshot(entry, hand);
            }));
            buttonOffsetX = 10;
        }
        this.addButton(new Button((width / 2) + buttonOffsetX, y, w, h, I18n.format("gui.strange.travel_journal.back"), (button) -> this.back()));
    }

    public static void takeScreenshot(Entry entry, Hand hand)
    {
        PacketHandler.sendToServer(new ServerTravelJournalAction(ServerTravelJournalAction.SCREENSHOT, entry, hand));
    }

    public static File getScreenshot(Entry entry)
    {
        return new File(new File(Minecraft.getInstance().gameDir, "screenshots"), entry.id);
    }

    private boolean hasScreenshot()
    {
        return file != null && file.exists();
    }

    private void back()
    {
        mc.displayGuiScreen(new TravelJournalScreen(player, hand));
    }
}

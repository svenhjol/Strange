package svenhjol.strange.feature.waypoints;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.api.event.HudRenderEvent;

public class WaypointsClient extends ClientFeature {
    private static DyeColor lastSeenColor; // Cached color of the last message.
    private static String lastSeenTitle; // Cached title of the last message.
    private static Component broadcastMessage = null; // Display message component when this is not null.
    private static int broadcastTime = 0; // Number of ticks that the message component has been shown.

    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return Waypoints.class;
    }

    @Override
    public void runWhenEnabled() {
        HudRenderEvent.INSTANCE.handle(this::handleHudRender);
    }

    public static void handleWaypointInfo(WaypointsNetwork.WaypointInfo message, Player player) {
        var random = player.getRandom();

        // Prevent spamming the client if the message and color are the same.
        var color = message.getColor();
        var playSound = message.playSound();
        var title = message.getTitle();
        var titleAsString = title.toString();

        if (lastSeenTitle != null
            && lastSeenColor != null
            && lastSeenTitle.equals(titleAsString)
            && lastSeenColor.equals(color)) return;

        lastSeenColor = color;
        lastSeenTitle = titleAsString;

        var displayTitle = (MutableComponent)title;

        if (playSound) {
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(Waypoints.broadcastSound.get(), 0.9f + (0.2f * random.nextFloat()), 0.35f));
        }

        var textColor = color.getFireworkColor() | 0x171717;
        broadcastMessage = displayTitle.withStyle(style -> style.withColor(textColor));
        broadcastTime = 12 * 60;
    }

    public static void handleFlushWaypoint(WaypointsNetwork.FlushWaypoint message, Player player) {
        lastSeenColor = null;
        lastSeenTitle = null;
    }

    private void handleHudRender(GuiGraphics guiGraphics, float tickDelta) {
        if (broadcastMessage == null || broadcastTime == 0) return;

        var minecraft = Minecraft.getInstance();
        var pose = guiGraphics.pose();
        var gui = minecraft.gui;
        var font = gui.getFont();
        var fade = broadcastTime - tickDelta;
        var col = 0xffffff;
        var len = 0;
        var brightness = (int)(fade * 255.0f / 80.0f);

        if (brightness > 255) {
            brightness = 255;
        }

        if (brightness > 8) {
            pose.pushPose();
            pose.translate(gui.screenWidth / 2.0F, 20, 0.0);

            len = font.width(broadcastMessage);
            if (len < 200) {
                pose.scale(1.4F, 1.4F, 1.4F);
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            col = brightness << 24 & 0xFF000000;
            gui.drawBackdrop(guiGraphics, font, -4, len, 0x808080 | col);

            var x = -len / 2;
            var y = -4;
            var color = 0xffffff | col;

            guiGraphics.drawString(font, broadcastMessage, x, y, color);

            RenderSystem.disableBlend();
            pose.popPose();
        }

        broadcastTime--;
        if (broadcastTime <= 0) {
            broadcastTime = 0;
            broadcastMessage = null;
        }
    }
}

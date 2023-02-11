package svenhjol.strange.feature.waypoints;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import svenhjol.charm.mixin.accessor.GuiAccessor;
import svenhjol.charm_api.event.HudRenderEvent;
import svenhjol.charm_core.annotation.ClientFeature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.strange.Strange;

import java.util.List;
import java.util.function.BooleanSupplier;

@ClientFeature
public class WaypointsClient extends CharmFeature {
    private static DyeColor lastSeenColor; // Cached color of the last message.
    private static String lastSeenTitle; // Cached title of the last message.
    private static Component broadcastMessage = null; // Display message component when this is not null.
    private static int broadcastTime = 0; // Number of ticks that the message component has been shown.

    @Override
    public List<BooleanSupplier> checks() {
        return List.of(() -> Strange.LOADER.isEnabled(Waypoints.class));
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
                SimpleSoundInstance.forUI(Waypoints.BROADCAST_SOUND.get(), 0.9F + (0.2F * random.nextFloat()), 0.35F));
        }

        var textColor = color.getFireworkColor() | 0x171717;
        broadcastMessage = displayTitle.withStyle(style -> style.withColor(textColor));
        broadcastTime = 12 * 60;
    }

    public static void handleFlushWaypoint(WaypointsNetwork.FlushWaypoint message, Player player) {
        lastSeenColor = null;
        lastSeenTitle = null;
    }

    private void handleHudRender(PoseStack poseStack, float tickDelta) {
        if (broadcastMessage == null || broadcastTime == 0) return;

        var minecraft = Minecraft.getInstance();
        var gui = minecraft.gui;
        var font = gui.getFont();
        var fade = broadcastTime - tickDelta;
        var col = 0xFFFFFF;
        var len = 0;
        var brightness = (int)(fade * 255.0f / 80.0f);

        if (brightness > 255) {
            brightness = 255;
        }

        if (brightness > 8) {
            poseStack.pushPose();
            poseStack.translate(((GuiAccessor)gui).getScreenWidth() / 2.0F, 20, 0.0);

            len = font.width(broadcastMessage);
            if (len < 200) {
                poseStack.scale(1.4F, 1.4F, 1.4F);
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            col = brightness << 24 & 0xFF000000;
            ((GuiAccessor)gui).invokeDrawBackdrop(poseStack, font, -4, len, 0x808080 | col);

            var x = -len / 2.0F;
            var y = -4.0F;
            var color = 0xFFFFFF | col;

            font.draw(poseStack, broadcastMessage, x, y, color);

            RenderSystem.disableBlend();
            poseStack.popPose();
        }

        broadcastTime--;
        if (broadcastTime <= 0) {
            broadcastTime = 0;
            broadcastMessage = null;
        }
    }
}

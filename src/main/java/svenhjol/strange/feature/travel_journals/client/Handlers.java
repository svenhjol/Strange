package svenhjol.strange.feature.travel_journals.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.feature.travel_journals.TravelJournalsClient;
import svenhjol.strange.feature.travel_journals.common.Networking;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class Handlers extends FeatureHolder<TravelJournalsClient> {
    private static final Component NEW_BOOKMARK = Component.translatable("gui.strange.travel_journals.new_bookmark");
    
    private Photo photo = null;

    public Handlers(TravelJournalsClient feature) {
        super(feature);
    }

    public void keyPress(String id) {
        if (Minecraft.getInstance().level == null) return;

        if (id.equals(feature().registers.makeBookmarkKey.get())) {
            Networking.C2SMakeBookmark.send(NEW_BOOKMARK.getString());
        }
    }

    public void takePhotoReceived(Player player, Networking.S2CTakePhoto packet) {
        photo = new Photo(packet.uuid());
        Minecraft.getInstance().setScreen(null);
    }

    public void clientTick(Minecraft minecraft) {
        if (photo != null) {
            if (!photo.isValid()) {
                scaleAndSendPhoto();
            } else {
                photo.tick();
            }
        }
    }

    public void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (photo != null && photo.isValid()) {
            photo.renderCountdown(guiGraphics);
        }
    }

    public void scaleAndSendPhoto() {
        log().debug("Preparing photo to send to server");
        var uuid = photo.uuid();
        photo = null;

        BufferedImage image;
        var path = new File(FabricLoader.getInstance().getGameDir() + "/screenshots/" + uuid + ".png");
        try {
            image = ImageIO.read(path);
        } catch (IOException e) {
            log().error("Could not read photo: " + e.getMessage());
            return;
        }

        // TODO: configurable scaling
        var scaledWidth = 192;
        var scaledHeight = 96;

        var scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        var graphics2D = scaledImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        graphics2D.dispose();

        boolean success;
        try {
            success = ImageIO.write(scaledImage, "png", path);
        } catch (IOException e) {
            log().error("Could not save resized photo: " + e.getMessage());
            return;
        }

        if (!success) {
            log().error("Writing image failed.");
            return;
        }

        Networking.C2SSendPhoto.send(uuid, scaledImage);
    }
}

package svenhjol.strange.feature.travel_journals.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.feature.travel_journals.TravelJournalsClient;
import svenhjol.strange.feature.travel_journals.client.screen.BookmarksScreen;
import svenhjol.strange.feature.travel_journals.common.BookmarkData;
import svenhjol.strange.feature.travel_journals.common.Networking;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class Handlers extends FeatureHolder<TravelJournalsClient> {
    private static final Component NEW_BOOKMARK = Component.translatable("gui.strange.travel_journals.new_bookmark");
    
    private Map<UUID, File> cachedScreenshots = new WeakHashMap<>();
    private Map<UUID, ResourceLocation> cachedPhotos = new WeakHashMap<>(); 
    private Photo photo = null;

    public Handlers(TravelJournalsClient feature) {
        super(feature);
    }

    public void keyPress(String id) {
        if (Minecraft.getInstance().level == null) return;

        if (id.equals(feature().registers.makeBookmarkKey.get())) {
            makeNewBookmark();
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

    public void makeNewBookmark() {
        Networking.C2SMakeBookmark.send(NEW_BOOKMARK.getString());
    }
    
    @SuppressWarnings("ConstantValue")
    @Nullable
    public ResourceLocation tryFetchPhoto(UUID uuid) {
        if (cachedPhotos.containsKey(uuid)) {
            var resource = cachedPhotos.get(uuid);
            if (resource != null) {
                return resource;
            }
        }
        
        if (!cachedScreenshots.containsKey(uuid)) {
            var file = screenshot(uuid);
            cachedScreenshots.put(uuid, file);
        }
        
        if (cachedScreenshots.containsKey(uuid)) {
            var file = cachedScreenshots.get(uuid);
            if (file == null) return null;
            
            try {
                var raf = new RandomAccessFile(file, "r");
                if (raf != null) {
                    raf.close();
                }

                var stream = new FileInputStream(file);
                var screenshot = NativeImage.read(stream);
                var dynamicTexture = new DynamicTexture(screenshot);
                var registeredTexture = Minecraft.getInstance().getTextureManager().register("screenshot", dynamicTexture);
                stream.close();
                
                cachedPhotos.put(uuid, registeredTexture);
                if (registeredTexture == null) {
                    throw new Exception("Problem with screenshot texture / registered texture");
                }
                
            } catch (Exception e) {
                log().error(e.getMessage());
            }
        }
        
        return null;
    }
    
    public void openBookmarks(ItemStack stack, int page) {
        Minecraft.getInstance().setScreen(new BookmarksScreen(stack, page));
    }

    public void openBookmark(BookmarkData bookmark) {
        // TODO
    }

    @Nullable
    private File screenshot(UUID uuid) {
        var minecraft = Minecraft.getInstance();
        var screenshotsDirectory = new File(minecraft.gameDirectory, "screenshots");
        var file = new File(screenshotsDirectory, uuid + ".png");
        return file.exists() ? file : null;
    }
}

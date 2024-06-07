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
    
    private final Map<UUID, ResourceLocation> cachedPhotos = new WeakHashMap<>();
    private final Map<UUID, Integer> fetchFromServer = new WeakHashMap<>();
    private Photo takingPhoto = null;

    public Handlers(TravelJournalsClient feature) {
        super(feature);
    }

    public void keyPress(String id) {
        if (Minecraft.getInstance().level == null) return;

        if (id.equals(feature().registers.makeBookmarkKey.get())) {
            makeNewBookmark();
        }
    }

    /**
     * Server wants the client to take a photo.
     */
    public void takePhotoReceived(Player player, Networking.S2CTakePhoto packet) {
        takingPhoto = new Photo(packet.uuid());
        Minecraft.getInstance().setScreen(null);
    }

    /**
     * Server providing photo to the client.
     */
    public void photoReceived(Player player, Networking.S2CPhoto packet) {
        trySavePhoto(packet.uuid(), packet.image());
    }

    public void clientTick(Minecraft minecraft) {
        if (takingPhoto != null) {
            if (!takingPhoto.isValid()) {
                scaleAndSendPhoto();
            } else {
                takingPhoto.tick();
            }
        }
    }

    public void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (takingPhoto != null && takingPhoto.isValid()) {
            takingPhoto.renderCountdown(guiGraphics);
        }
    }

    public void scaleAndSendPhoto() {
        log().debug("Preparing photo to send to server");
        var uuid = takingPhoto.uuid();
        takingPhoto = null;

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

        Networking.C2SPhoto.send(uuid, scaledImage);
    }

    public void makeNewBookmark() {
        Networking.C2SMakeBookmark.send(NEW_BOOKMARK.getString());
    }
    
    public void trySavePhoto(UUID uuid, BufferedImage image) {
        var minecraft = Minecraft.getInstance();
        var dir = new File(minecraft.gameDirectory, "screenshots");
        var path = new File(dir, uuid + ".png");
        boolean success;
        
        try {
            success = ImageIO.write(image, "png", path);
        } catch (IOException e) {
            log().error("Could not save photo for uuid " + uuid + ": " + e.getMessage());
            return;
        }
        
        if (success) {
            log().debug("Saved image to screenshots for uuid: " + uuid);
        } else {
            log().error("ImageIO.write did not save the image successfully for uuid: " + uuid);
        }
    }
    
    @SuppressWarnings("ConstantValue")
    @Nullable
    public ResourceLocation tryLoadPhoto(UUID uuid) {
        // Check for cached photo data, use if present.
        if (cachedPhotos.containsKey(uuid)) {
            var resource = cachedPhotos.get(uuid);
            if (resource != null) {
                return resource;
            }
        }
        
        // Checks server download.
        if (fetchFromServer.containsKey(uuid)) {
            var ticks = fetchFromServer.getOrDefault(uuid, 0);
            
            if (ticks == -1) {
                // Failed permanently.
                return null;
            }
            
            if (ticks == 0) {
                Networking.C2SDownloadPhoto.send(uuid); // Request photo from the server.
                log().debug("Requesting image from the server for uuid " + uuid);
            }
            
            if (ticks < 20) {
                // Continue to wait.
                ++ticks;
                fetchFromServer.put(uuid, ticks);
                return null;
            }
        }
        
        // Try to get the screenshot locally.
        var file = localScreenshot(uuid);
        if (file == null) {
            var ticks = fetchFromServer.getOrDefault(uuid, 0);
            if (ticks > 0) {
                // If we have previously tried to fetch from the server and it failed, then give up.
                log().error("Couldn't download image from the server, giving up. uuid: " + uuid);
                fetchFromServer.put(uuid, -1);
                return null;
            }
            
            // Can't find locally, rigger a download from the server.
            fetchFromServer.put(uuid, 0);
            log().debug("Couldn't find image locally, scheduling server download. uuid: " + uuid);
            return null;
        }
        
        // Open local screenshot file, load dynamic texture into cache.
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
        
        return null;
    }
    
    public void clearPhotoCache() {
        cachedPhotos.clear();
        fetchFromServer.clear();
    }
    
    public void openBookmarks(ItemStack stack, int page) {
        clearPhotoCache();
        Minecraft.getInstance().setScreen(new BookmarksScreen(stack, page));
    }

    public void openBookmark(BookmarkData bookmark) {
        // TODO
    }

    @Nullable
    private File localScreenshot(UUID uuid) {
        var minecraft = Minecraft.getInstance();
        var screenshotsDirectory = new File(minecraft.gameDirectory, "screenshots");
        var file = new File(screenshotsDirectory, uuid + ".png");
        return file.exists() ? file : null;
    }
}

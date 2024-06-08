package svenhjol.strange.feature.travel_journals.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.feature.travel_journals.TravelJournals;
import svenhjol.strange.feature.travel_journals.TravelJournalsClient;
import svenhjol.strange.feature.travel_journals.client.screen.BookmarkDetailScreen;
import svenhjol.strange.feature.travel_journals.client.screen.BookmarksScreen;
import svenhjol.strange.feature.travel_journals.common.BookmarkData;
import svenhjol.strange.feature.travel_journals.common.Helpers;
import svenhjol.strange.feature.travel_journals.common.JournalData;
import svenhjol.strange.feature.travel_journals.common.Networking;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class Handlers extends FeatureHolder<TravelJournalsClient> {
    private final Map<UUID, ResourceLocation> cachedPhotos = new WeakHashMap<>();
    private final Map<UUID, Integer> fetchFromServer = new WeakHashMap<>();
    private final Map<UUID, Integer> savedJournalPagination = new WeakHashMap<>();
    private Photo takingPhoto = null;
    private boolean hasMap;
    private boolean hasPaper;
    private long lastExportOperation = 0;

    public Handlers(TravelJournalsClient feature) {
        super(feature);
    }

    /**
     * Listen to client key presses.
     */
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
        takingPhoto = new Photo(packet.journalId(), packet.photoId());
        Minecraft.getInstance().setScreen(null);
    }

    /**
     * Server providing photo to the client.
     */
    public void photoReceived(Player player, Networking.S2CPhoto packet) {
        trySavePhoto(packet.uuid(), packet.image());
    }

    /**
     * Tick the client to do the countdown and take a photo.
     */
    public void clientTick(Minecraft minecraft) {
        if (takingPhoto != null) {
            if (!takingPhoto.isValid()) {
                // Get the journal by its ID and open the bookmark page.
                var stack = Helpers.tryGetTravelJournal(minecraft.player, takingPhoto.journalId());
                if (!stack.isEmpty()) {
                    openBookmark(stack, takingPhoto.photoId());
                }
                
                // Downscale and send photo to server asynchronously.
                scaleAndSendPhoto();
            } else {
                takingPhoto.tick();
            }
        }
        
        if (minecraft.level != null && minecraft.player != null) {
            // Poll inventory for paper and map when on the bookmark page.
            if (minecraft.level.getGameTime() % 20 == 0 && minecraft.screen instanceof BookmarkDetailScreen) {
                hasMap = minecraft.player.getInventory().contains(new ItemStack(Items.MAP));
                hasPaper = minecraft.player.getInventory().contains(new ItemStack(Items.PAPER));
            }
        }
    }

    /**
     * Render the HUD for the photo countdown.
     */
    public void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (takingPhoto != null && takingPhoto.isValid()) {
            takingPhoto.renderCountdown(guiGraphics);
        }
    }

    /**
     * Downscale the screenshot/photo PNG and send the image to the server for the new bookmark.
     */
    public void scaleAndSendPhoto() {
        var uuid = takingPhoto.photoId();
        takingPhoto = null;
        
        log().debug("Preparing photo to send to server for photoId: " + uuid);
        BufferedImage image;
        var dir = getOrCreatePhotosDir();
        var path = new File(dir, uuid + ".png");
        try {
            image = ImageIO.read(path);
        } catch (IOException e) {
            log().error("Could not read photo for photoId " + uuid + ": " + e.getMessage());
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
            log().error("Could not save resized photo for photoId " + uuid + ": " + e.getMessage());
            return;
        }

        if (!success) {
            log().error("Writing image failed for photoId: " + uuid);
            return;
        }

        Networking.C2SPhoto.send(uuid, scaledImage);
    }

    /**
     * Create a new bookmark.
     * Called when the player presses the bookmark key or clicks the "New bookmark" button.
     */
    public void makeNewBookmark() {
        var minecraft = Minecraft.getInstance();
        Component bookmarkName;
        
        if (minecraft.player != null) {
            var biomeName = Component.translatable(biomeLocaleKey(minecraft.player)).getString();
            bookmarkName = Component.translatable("gui.strange.travel_journals.default_name", biomeName);
        } else {
            bookmarkName = Component.translatable("gui.strange.travel_journals.new_bookmark");
        }

        Networking.C2SMakeBookmark.send(bookmarkName.getString());
    }

    /**
     * Update a bookmark.
     * Pass the journal stack that this bookmark belongs to.
     */
    public void updateBookmark(ItemStack stack, BookmarkData bookmark) {
        var journal = JournalData.get(stack);

        // Update stack's local state.
        new JournalData.Mutable(journal)
            .updateBookmark(bookmark)
            .save(stack);

        // Sync the bookmark with the server.
        log().debug("Sending updated bookmark for photoId: " + bookmark.id());
        Networking.C2SUpdateBookmark.send(journal.id(), bookmark);
    }

    /**
     * Delete a bookmark.
     * Pass the journal stack that this bookmark belongs to.
     */
    public void deleteBookmark(ItemStack stack, BookmarkData bookmark) {
        var journal = JournalData.get(stack);

        // Update stack's local state.
        new JournalData.Mutable(journal)
            .deleteBookmark(bookmark.id())
            .save(stack);

        // Sync the bookmark with the server.
        log().debug("Sending deleted bookmark for photoId: " + bookmark.id());
        Networking.C2SDeleteBookmark.send(journal.id(), bookmark.id());
        
        // Clean up local photos.
        tryDeletePhoto(bookmark.id());
    }

    /**
     * Try and save a given image buffer to a file within the custom photos directory.
     */
    public void trySavePhoto(UUID uuid, BufferedImage image) {
        var dir = getOrCreatePhotosDir();
        var path = new File(dir, uuid + ".png");
        boolean success;
        
        try {
            success = ImageIO.write(image, "png", path);
        } catch (IOException e) {
            log().error("Could not save photo for photoId " + uuid + ": " + e.getMessage());
            return;
        }
        
        if (success) {
            log().debug("Saved image to photos for photoId: " + uuid);
        } else {
            log().error("ImageIO.write did not save the image successfully for photoId: " + uuid);
        }
    }

    /**
     * Try and get a texture resource location for a given photo UUID.
     * If a photo can't be loaded locally, we make an asynchronous request to the server
     * to download the photo if available. After a certain number of ticks we check to
     * see if the photo is now downloaded to the client. This process will only attempt
     * a single server call. To try again, call clearPhotoCache().
     * While a photo isn't available, a placeholder is used.
     */
    @SuppressWarnings("ConstantValue")
    public ResourceLocation tryLoadPhoto(UUID uuid) {
        var def = Resources.PHOTO_BACKGROUND;
        
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
                return def;
            }
            
            if (ticks == 0) {
                Networking.C2SDownloadPhoto.send(uuid); // Request photo from the server.
                log().debug("Requesting image from the server for photoId: " + uuid);
            }
            
            if (ticks < 20) {
                // Continue to wait.
                ++ticks;
                fetchFromServer.put(uuid, ticks);
                return def;
            }
        }
        
        // Try to get the photo locally, falling back to server download.
        var file = localPhoto(uuid);
        if (file == null) {
            var ticks = fetchFromServer.getOrDefault(uuid, 0);
            if (ticks > 0) {
                // If we have previously tried to fetch from the server and it failed, then give up.
                log().error("Couldn't download image from the server, giving up. photoId: " + uuid);
                fetchFromServer.put(uuid, -1);
                return def;
            }
            
            // Can't find locally, trigger a download from the server.
            fetchFromServer.put(uuid, 0);
            log().debug("Couldn't find image locally, scheduling server download. photoId: " + uuid);
            return def;
        }
        
        // Open local photo file, load dynamic texture into cache.
        try {
            var raf = new RandomAccessFile(file, "r");
            if (raf != null) {
                raf.close();
            }

            var stream = new FileInputStream(file);
            var photo = NativeImage.read(stream);
            var dynamicTexture = new DynamicTexture(photo);
            var registeredTexture = Minecraft.getInstance().getTextureManager().register("stange_photo", dynamicTexture);
            stream.close();
            
            cachedPhotos.put(uuid, registeredTexture);
            if (registeredTexture == null) {
                throw new Exception("Problem with image texture / registered texture for photoId: " + uuid);
            }
            
        } catch (Exception e) {
            log().error(e.getMessage());
        }
        
        return def;
    }

    /**
     * Try and delete an image from the custom photos directory for given photo UUID.
     */
    public void tryDeletePhoto(UUID uuid) {
        var dir = getOrCreatePhotosDir();
        var path = new File(dir, uuid + ".png");
        
        if (path.exists()) {
            var result = path.delete();
            if (result) {
                log().debug("Deleted photo with photoId: " + uuid);
            } else {
                log().error("Error trying to delete photo with photoId: " + uuid);
            }
        }
    }

    /**
     * Clear cached photo image textures and server download attempts.
     */
    public void clearPhotoCache() {
        cachedPhotos.clear();
        fetchFromServer.clear();
    }

    /**
     * Open the bookmarks screen.
     * @param stack The travel journal in which the bookmarks should be loaded from.
     */
    public void openBookmarks(ItemStack stack) {
        var uuid = JournalData.get(stack).id();
        var page = savedJournalPagination.getOrDefault(uuid, 1);
        openBookmarks(stack, page);
    }
    
    public void openBookmarks(ItemStack stack, int page) {
        clearPhotoCache();
        var uuid = JournalData.get(stack).id();
        savedJournalPagination.put(uuid, page);
        Minecraft.getInstance().setScreen(new BookmarksScreen(stack, page));
    }
    
    public void openBookmark(ItemStack stack, UUID bookmarkId) {
        JournalData.get(stack)
            .getBookmark(bookmarkId)
            .ifPresent(bookmark -> openBookmark(stack, bookmark));
    }

    public void openBookmark(ItemStack stack, BookmarkData bookmark) {
        Minecraft.getInstance().setScreen(new BookmarkDetailScreen(stack, bookmark));
    }

    /**
     * Gets or returns the custom photos directory.
     * We don't want store strange's scaled photos directly inside minecraft's screenshots folder.
     * Create a subdirectory to store all our custom things in.
     */
    public File getOrCreatePhotosDir() {
        var minecraft = Minecraft.getInstance();
        var defaultDir = new File(minecraft.gameDirectory, "screenshots");
        var photosDir = new File(defaultDir, TravelJournals.PHOTOS_DIR);
        
        if (!photosDir.exists() && !photosDir.mkdir()) {
            throw new RuntimeException("Could not create custom photos directory in the screenshots folder, giving up");
        }
        
        return photosDir;
    }

    /**
     * Moves a screenshot into the custom photos folder.
     * Typically this is done after taking a screenshot Screenshot.grab().
     */
    public void moveScreenshotIntoPhotosDir(UUID uuid) {
        var minecraft = Minecraft.getInstance();
        var defaultDir = new File(minecraft.gameDirectory, "screenshots");
        var photosDir = getOrCreatePhotosDir();
        
        var copyFrom = new File(defaultDir, uuid + ".png");
        var copyTo = new File(photosDir, uuid + ".png");

        try {
            Files.move(copyFrom.toPath(), copyTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log().error("Could not move screenshot into photos dir for photoId " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Get a locale key for a dimension.
     */
    public String dimensionLocaleKey(ResourceKey<Level> dimension) {
        var location = dimension.location();
        var namespace = location.getNamespace();
        var path = location.getPath();
        return "dimension." + namespace + "." + path;
    }

    /**
     * Get a locale key for the biome at the player's current position.
     */
    public String biomeLocaleKey(Player player) {
        var registry = player.level().registryAccess();
        var biome = player.level().getBiome(player.blockPosition());
        var key = registry.registryOrThrow(Registries.BIOME).getKey(biome.value());
        
        if (key == null) {
            throw new RuntimeException("Can't get player biome");
        }

        var namespace = key.getNamespace();
        var path = key.getPath();
        return "biome." + namespace + "." + path;
    }

    public boolean hasMap() {
        return hasMap;
    }

    public boolean hasPaper() {
        return hasPaper;
    }

    public void exportBookmark(BookmarkData bookmark) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            var time = minecraft.level.getGameTime();
            if (lastExportOperation == 0 || time - lastExportOperation > 10) {
                Networking.C2SExportBookmark.send(bookmark);
                lastExportOperation = time;
            }
        }
    }

    public void exportMap(BookmarkData bookmark) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            var time = minecraft.level.getGameTime();
            if (lastExportOperation == 0 || time - lastExportOperation > 10) {
                Networking.C2SExportMap.send(bookmark);
                lastExportOperation = time;
            }
        }
    }
    
    /**
     * Gets a file reference to a photo on the client's device.
     */
    @Nullable
    public File localPhoto(UUID uuid) {
        var file = new File(getOrCreatePhotosDir(), uuid + ".png");
        return file.exists() ? file : null;
    }
}

package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.feature.travel_journals.TravelJournals;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Handlers extends FeatureHolder<TravelJournals> {
    public static final LevelResource PHOTOS_DIR_RESOURCE = new LevelResource(TravelJournals.PHOTOS_DIR);

    public Handlers(TravelJournals feature) {
        super(feature);
    }

    /**
     * Client wants to make a new bookmark.
     */
    public void makeBookmarkReceived(Player player, Networking.C2SMakeBookmark packet) {
        var dimension = player.level().dimension();
        var pos = player.blockPosition();
        UUID bookmarkId;
        
        var opt = tryGetTravelJournal(player);
        if (opt.isEmpty()) return;
        var stack = opt.get();
        
        var journal = JournalData.get(stack);
        if (journal.isFull()) {
            return; // TODO: message back to client.
        }
        
        // Create a new bookmark and attach it to the journal.
        var bookmark = BookmarkData.create()
            .name(packet.name())
            .dimension(dimension)
            .pos(pos)
            .timestamp(System.currentTimeMillis() / 1000L)
            .author(player.getScoreboardName())
            .toImmutable();

        bookmarkId = bookmark.id();
        log().debug("Created a new bookmark entry with UUID " + bookmarkId);

        new JournalData.Mutable(journal)
            .addBookmark(bookmark)
            .save(stack);
        
        // Instruct the client to take a photo.
        Networking.S2CTakePhoto.send((ServerPlayer)player, bookmarkId);
    }

    /**
     * Client has sent a new photo.
     */
    public void photoReceived(Player player, Networking.C2SPhoto packet) {
        trySavePhoto((ServerLevel) player.level(), packet.uuid(), packet.image());
    }

    /**
     * Client wants to download a photo.
     */
    public void downloadPhotoReceived(Player player, Networking.C2SDownloadPhoto packet) {
        var uuid = packet.uuid();
        var image = tryLoadPhoto((ServerLevel) player.level(), uuid);
        if (image == null) {
            return;
        }
        
        // Send the photo to the client.
        Networking.S2CPhoto.send((ServerPlayer) player, uuid, image);
    }
    
    /**
     * Gets or returns the custom photos directory.
     * Create a subdirectory within the world folder to store all our custom photos in.
     */
    public File getOrCreatePhotosDir(ServerLevel level) {
        var server = level.getServer();
        var photosDir = server.getWorldPath(PHOTOS_DIR_RESOURCE).toFile();
        
        if (!photosDir.exists() && !photosDir.mkdir()) {
            throw new RuntimeException("Could not create photos directory in the world folder, giving up");
        }
        return photosDir;
    }

    /**
     * Try and save a given image buffer to a file within the custom photos directory.
     */
    public void trySavePhoto(ServerLevel level, UUID uuid, BufferedImage image) {
        var dir = getOrCreatePhotosDir(level);
        var path = new File(dir, uuid + ".png");
        boolean success;

        try {
            success = ImageIO.write(image, "png", path);
        } catch (IOException e) {
            log().error("Could not save photo for uuid: " + uuid + ": " + e.getMessage());
            return;
        }

        if (success) {
            log().debug("Saved image to photos for uuid: " + uuid);
        } else {
            log().error("ImageIO.write did not save the image successfully for uuid: " + uuid);
        }
    }

    /**
     * Try and load an image from the custom photos directory for a given photo UUID.
     */
    @Nullable
    public BufferedImage tryLoadPhoto(ServerLevel level, UUID uuid) {
        var dir = getOrCreatePhotosDir(level);
        var path = new File(dir, uuid + ".png");
        BufferedImage image;
        
        try {
            image = ImageIO.read(path);
        } catch (IOException e) {
            log().error("Could not load photo for uuid: " + uuid + ": " + e.getMessage());
            return null;
        }

        return image;
    }

    /**
     * Fetch the most readily available travel journal held by the player.
     * The order that is checked is:
     * - mainhand
     * - offhand
     * - inventory slots starting from 0
     */
    public Optional<ItemStack> tryGetTravelJournal(Player player) {
        var inventory = player.getInventory();
        List<ItemStack> items = new ArrayList<>();

        for (InteractionHand hand : InteractionHand.values()) {
            items.add(player.getItemInHand(hand));
        }
        
        items.addAll(inventory.items);
        
        for (var stack : items) {
            if (stack.is(feature().registers.item.get())) {
                return Optional.of(stack);
            }
        }

        return Optional.empty();
    }
}

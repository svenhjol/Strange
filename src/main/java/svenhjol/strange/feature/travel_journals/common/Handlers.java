package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelResource;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.feature.travel_journals.TravelJournals;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
        
        var stack = Helpers.tryGetTravelJournal(player);
        if (stack.isEmpty()) return;

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

        log().debug("Created a new bookmark entry with UUID " + bookmark.id());

        new JournalData.Mutable(journal)
            .addBookmark(bookmark)
            .save(stack);
        
        // Instruct the client to take a photo.
        Networking.S2CTakePhoto.send((ServerPlayer)player, journal.id(), bookmark.id());
    }
    
    public void updateBookmarkReceived(Player player, Networking.C2SUpdateBookmark packet) {
        var stack = Helpers.tryGetTravelJournal(player, packet.journalId());
        if (stack.isEmpty()) {
            log().error("No such journal?");
            return;
        }

        new JournalData.Mutable(JournalData.get(stack))
            .updateBookmark(packet.bookmark())
            .save(stack);
    }
    
    public void deleteBookmarkReceived(Player player, Networking.C2SDeleteBookmark packet) {
        var stack = Helpers.tryGetTravelJournal(player, packet.journalId());
        if (stack.isEmpty()) {
            log().error("No such journal?");
            return;
        }
        
        new JournalData.Mutable(JournalData.get(stack))
            .deleteBookmark(packet.bookmarkId())
            .save(stack);
        
        // Clean up photo on server.
        tryDeletePhoto((ServerLevel) player.level(), packet.bookmarkId());
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

    public void exportBookmarkReceived(Player player, Networking.C2SExportBookmark packet) {
        // TODO
    }

    public void exportMapReceived(Player player, Networking.C2SExportMap packet) {
        var level = (ServerLevel)player.level();
        var bookmark = packet.bookmark();
        var dimension = bookmark.dimension();
        var pos = bookmark.pos();
        var name = bookmark.name();
        
        if (level.dimension() != dimension) {
            return;
        }
        
        for (ItemStack stack : Helpers.collectPotentialItems(player)) {
            if (stack.is(Items.MAP)) {
                var map = MapItem.create(level, pos.getX(), pos.getZ(), (byte)2, true, true);
                MapItem.renderBiomePreviewMap(level, map);
                MapItemSavedData.addTargetDecoration(map, pos, name, MapDecorationTypes.GRAY_BANNER);
                map.set(DataComponents.ITEM_NAME, Component.literal(name));
                stack.shrink(1);
                player.addItem(map);
                level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS);
                return;
            }
        }
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
            log().error("Could not save photo for photoId: " + uuid + ": " + e.getMessage());
            return;
        }

        if (success) {
            log().debug("Saved image to photos for photoId: " + uuid);
        } else {
            log().error("ImageIO.write did not save the image successfully for photoId: " + uuid);
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
            log().error("Could not load photo for photoId: " + uuid + ": " + e.getMessage());
            return null;
        }

        return image;
    }

    /**
     * Try and delete an image from the custom photos directory for given photo UUID.
     */
    public void tryDeletePhoto(ServerLevel level, UUID uuid) {
        var dir = getOrCreatePhotosDir(level);
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
}

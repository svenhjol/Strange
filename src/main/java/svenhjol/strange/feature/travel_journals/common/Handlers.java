package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.ItemLore;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Handlers extends FeatureHolder<TravelJournals> {
    private static final boolean DO_DELETE_ON_SERVER = false;
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

    /**
     * Client wants to update the given bookmark.
     * Check that the given journal is present in the player's inventory.
     */
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

    /**
     * Client wants to delete the given bookmark.
     * Check that the given journal is present in the player's inventory.
     */
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
        if (DO_DELETE_ON_SERVER) {
            tryDeletePhoto((ServerLevel) player.level(), packet.bookmarkId());
        }
    }

    /**
     * Client has sent a new photo.
     */
    public void photoReceived(Player player, Networking.C2SPhoto packet) {
        trySavePhoto((ServerLevel) player.level(), packet.bookmarkId(), packet.image());
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
     * Clients wants to export a map using the given bookmark.
     */
    public void exportMapReceived(Player player, Networking.C2SExportMap packet) {
        var level = (ServerLevel)player.level();
        var bookmark = packet.bookmark();
        var dimension = bookmark.dimension();
        var pos = bookmark.pos();
        
        if (level.dimension() != dimension) {
            return;
        }
        
        for (var stack : Helpers.collectPotentialItems(player)) {
            if (stack.is(Items.MAP)) {
                var map = MapItem.create(level, pos.getX(), pos.getZ(), (byte)2, true, true);
                MapItem.renderBiomePreviewMap(level, map);
                MapItemSavedData.addTargetDecoration(map, pos, "+", MapDecorationTypes.TARGET_X);
                setCommonDataAndGiveToPlayer(player, map, bookmark);
                stack.shrink(1);
                return;
            }
        }
    }
    
    /**
     * Clients wants to export a page using the given bookmark.
     */
    public void exportPageReceived(Player player, Networking.C2SExportPage packet) {
        var bookmark = packet.bookmark();

        for (var stack : Helpers.collectPotentialItems(player)) {
            if (stack.is(Items.PAPER)) {
                var page = new ItemStack(feature().registers.travelJournalPageItem.get());
                page.set(feature().registers.bookmarkData.get(), bookmark);
                setCommonDataAndGiveToPlayer(player, page, bookmark);
                stack.shrink(1);
                return;
            }
        }
    }

    /**
     * Shared method to add the bookmark name as the item name and extra details as the item lore.
     */
    private void setCommonDataAndGiveToPlayer(Player player, ItemStack stack, BookmarkData bookmark) {
        var name = bookmark.name();
        var description = bookmark.extra().description();
        
        stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        List<Component> lore = new ArrayList<>();
        
        // Add dimension and position to tooltip.
        lore.add(Helpers.dimensionAsText(bookmark.dimension()).copy()
            .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withItalic(false)));
        lore.add(Helpers.positionAsText(bookmark.pos()).copy()
            .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withItalic(false)));
        
        // Add the full description to tooltip, if present.
        if (!description.isEmpty()) {
            lore.addAll(Helpers.wrap(description));
        }
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        player.addItem(stack);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6f, 0.95f);
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
            log().error("Could not save photo for bookmarkId: " + uuid + ": " + e.getMessage());
            return;
        }

        if (success) {
            log().debug("Saved image to photos for bookmarkId: " + uuid);
        } else {
            log().error("ImageIO.write did not save the image successfully for bookmarkId: " + uuid);
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
            log().warnIfDebug("Could not load photo for bookmarkId: " + uuid + ": " + e.getMessage());
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
                log().debug("Deleted photo with bookmarkId: " + uuid);
            } else {
                log().error("Error trying to delete photo with bookmarkId: " + uuid);
            }
        }
    }
}

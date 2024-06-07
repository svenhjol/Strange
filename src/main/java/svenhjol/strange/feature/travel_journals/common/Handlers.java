package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.feature.travel_journals.TravelJournals;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Handlers extends FeatureHolder<TravelJournals> {
    public static final LevelResource PHOTOS_DIR = new LevelResource("strange_travel_journal_photos");

    public Handlers(TravelJournals feature) {
        super(feature);
    }

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
        
        // Instruct the client to take a screenshot.
        Networking.S2CTakePhoto.send((ServerPlayer)player, bookmarkId);
    }

    public void sendPhotoReceived(Player player, Networking.C2SSendPhoto packet) {
        log().debug(packet.toString());
        var result = trySavePhoto((ServerLevel) player.level(), packet.uuid(), packet.image());
        if (!result) {
           log().error("Photo for " + packet.uuid() + " failed.");
        }
    }

    public File getOrCreatePhotosDir(ServerLevel level) {
        var server = level.getServer();
        var photosDir = server.getWorldPath(PHOTOS_DIR).toFile();
        if (!photosDir.exists() && !photosDir.mkdir()) {
            throw new RuntimeException("Could not create photos directory in the world folder");
        }
        return photosDir;
    }

    public boolean trySavePhoto(ServerLevel level, UUID uuid, BufferedImage image) {
        var dir = getOrCreatePhotosDir(level);
        var path = new File(dir + "/" + uuid + ".png");
        boolean success;

        try {
            success = ImageIO.write(image, "png", path);
        } catch (IOException e) {
            log().error("Error saving photo: " + e.getMessage());
            return false;
        }

        return success;
    }

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

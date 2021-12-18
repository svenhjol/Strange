package svenhjol.strange.module.bookmarks;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.network.BookmarkMessages;

import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID)
public class Bookmarks extends CharmModule {
    private static BookmarkData bookmarkData;

    public static Optional<BookmarkData> getBookmarks() {
        return Optional.ofNullable(bookmarkData);
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayNetworking.registerGlobalReceiver(BookmarkMessages.SERVER_SYNC_BOOKMARKS, this::handleSyncBookmarks);
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {

        // Overworld is loaded first. We set up the bookmarks storage here.
        if (level.dimension() == Level.OVERWORLD) {
            ServerLevel overworld = (ServerLevel) level;
            DimensionDataStorage storage = overworld.getDataStorage();

            bookmarkData = storage.computeIfAbsent(
                tag -> BookmarkData.load(overworld, tag),
                () -> new BookmarkData(overworld),
                BookmarkData.getFileId(level.dimensionType())
            );
        }
    }

    private void handleSyncBookmarks(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        getBookmarks().ifPresent(bookmarks -> {
            CompoundTag tag = new CompoundTag();
            bookmarks.bookmarks.save(tag);
            NetworkHelper.sendPacketToClient(player, BookmarkMessages.CLIENT_SYNC_BOOKMARKS, buf -> buf.writeNbt(tag));
        });
    }
}
